package com.co.service;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.co.binding.CoResponse;
import com.co.entity.CitizenAppEntity;
import com.co.entity.CoTriggerEntity;
import com.co.entity.DCCaseEntity;
import com.co.entity.EligibilityDetailEntity;
import com.co.repository.CaseEntityRepository;
import com.co.repository.CitizenAppRepository;
import com.co.repository.CoTriggerEntityRepository;
import com.co.repository.EligibilityDetailEntityRepository;
import com.co.utils.EmailUtils;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class CoServiceImpl implements CoService {

	@Autowired
	private CoTriggerEntityRepository triggerEntityRepository;

	@Autowired
	private EligibilityDetailEntityRepository detailEntityRepository;

	@Autowired
	private CitizenAppRepository appRepository;

	@Autowired
	private CaseEntityRepository caseEntityRepository;
	
	@Autowired
	private EmailUtils emailUtils;

	@Override
	public CoResponse processPendingTriggers() {
		
		Long failed = 0L;
		Long success = 0L;
		
		CoResponse response = new CoResponse();

		// fetch all pending triggers
		List<CoTriggerEntity> pendingTrgs = triggerEntityRepository.findByTrgStatus("Pending");
		
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		ExecutorCompletionService<Object> pool = new ExecutorCompletionService<>(executorService);
		
		response.setTotalTriggers(Long.valueOf(pendingTrgs.size()));

		// process each pending trigger
		for (CoTriggerEntity entity : pendingTrgs) {
			
			pool.submit(new Callable<Object>() {
				
				@Override
				public Object call() throws Exception {
					
					try {
						processTrigger(response, entity);
//						success++;
					} catch (Exception e) {
						e.printStackTrace();
//						failed++;
					}
					
					return null;
				}
			});	
		}
		
		response.setSuccTriggers(success);
		response.setFailedTriggers(failed);
		
		return response;
	}
	
	private CitizenAppEntity processTrigger(CoResponse response, CoTriggerEntity entity) throws Exception {
		
		CitizenAppEntity appEntity = null;
		
		// get eligibility data based on caseNum
		EligibilityDetailEntity eligi = detailEntityRepository.findByCaseNum(entity.getCaseNum());

		// get citizen data based on caseNum
		Optional<DCCaseEntity> findById = caseEntityRepository.findById(entity.getCaseNum());

		if (findById.isPresent()) { 
			
			DCCaseEntity dcCaseEntity = findById.get();
			Integer appId = dcCaseEntity.getAppId();
			Optional<CitizenAppEntity> appEntityOptional = appRepository.findById(appId);

			if (appEntityOptional.isPresent()) {
				appEntity = appEntityOptional.get();
			}
			
		}
		
		generateAndSendPdf(eligi, appEntity);
		
		return appEntity;
	}

	private void generateAndSendPdf(EligibilityDetailEntity eligiData, CitizenAppEntity appEntity) throws Exception {
		
		System.out.println("Plan Start Date: " + eligiData.getPlanStartDate());
		System.out.println("Plan End Date: " + eligiData.getPlanEndDate());
		System.out.println("Benefit Amount: " + eligiData.getBenefitAmt());
		System.out.println("Denial Reason: " + eligiData.getDenialReason());
		
		Document document = new Document(PageSize.A4);
		
		File file = new File(eligiData.getCaseNum()+".pdf");

		FileOutputStream fos = null;
		
		try {
			fos = new FileOutputStream(file);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}

		PdfWriter.getInstance(document, fos);

		document.open();
		Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(18);
		font.setColor(Color.BLUE);

		Paragraph paragraph = new Paragraph("Eligibility Report", font);
		paragraph.setAlignment(Paragraph.ALIGN_CENTER);

		document.add(paragraph);

		PdfPTable pdfPTable = new PdfPTable(7);
		pdfPTable.setWidthPercentage(100f);
		pdfPTable.setWidths(new float[] { 1.5f, 3.5f, 3.0f, 1.5f, 3.0f, 1.5f, 3.0f });
		pdfPTable.setSpacingBefore(10);

		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(Color.BLUE);
		cell.setPadding(5);

		font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setColor(Color.WHITE);

		cell.setPhrase(new Phrase("Citizen Name", font));
		pdfPTable.addCell(cell);

		cell.setPhrase(new Phrase("Plan Name", font));
		pdfPTable.addCell(cell);

		cell.setPhrase(new Phrase("Plan Status", font));
		pdfPTable.addCell(cell);

		cell.setPhrase(new Phrase("Plan START DATE", font));
		pdfPTable.addCell(cell);

		cell.setPhrase(new Phrase("Plan END DATE", font));
		pdfPTable.addCell(cell);

		cell.setPhrase(new Phrase("Benefit AMT", font));
		pdfPTable.addCell(cell);

		cell.setPhrase(new Phrase("Denial Reason", font));
		pdfPTable.addCell(cell);

		pdfPTable.addCell(appEntity.getFullName());
		pdfPTable.addCell(eligiData.getPlanName());
		pdfPTable.addCell(eligiData.getPlanStatus());
		pdfPTable.addCell(String.valueOf(eligiData.getPlanStartDate()));
		pdfPTable.addCell(String.valueOf(eligiData.getPlanEndDate()));
		pdfPTable.addCell(String.valueOf(eligiData.getBenefitAmt()));
		pdfPTable.addCell(eligiData.getDenialReason());

		document.add(pdfPTable);
		document.close();
		
		String subject = "HIS Eligibility Info";
		String body = "HIS Eligibility Info";
		
		emailUtils.sendEmail(subject, body, appEntity.getEmail().trim(), file);

		updateTrigger(eligiData.getCaseNum(), file);
		
		file.delete();
		
	}
	
	private void updateTrigger(Long caseNum, File file) throws Exception {
		
		CoTriggerEntity coEntity = triggerEntityRepository.findByCaseNum(caseNum);
		
		byte[] arr = new byte[(byte) file.length()];
		
		FileInputStream fis = new FileInputStream(file);
		fis.read(arr);
		
		coEntity.setCoPdf(arr);
		coEntity.setTrgStatus("Completed");
		
		triggerEntityRepository.save(coEntity);
		
		fis.close();
	}
	
}
