package com.mezei.aml.ingest.converter;

import com.mezei.aml.ingest.exception.IsoParseException;
import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import iso.std.iso._20022.tech.xsd.pacs_008_001.CreditTransferTransaction70;
import iso.std.iso._20022.tech.xsd.pacs_008_001.Document;
import iso.std.iso._20022.tech.xsd.pacs_008_001.FIToFICustomerCreditTransferV13;
import iso.std.iso._20022.tech.xsd.pacs_008_001.GroupHeader131;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;

@Component
public class Iso20022Parser {
    private final JAXBContext jaxbContext;
    private final IsoPacs008Mapper isoPacs008Mapper;

    public Iso20022Parser(IsoPacs008Mapper isoPacs008Mapper) throws JAXBException {
        this.isoPacs008Mapper = isoPacs008Mapper;
        this.jaxbContext = JAXBContext.newInstance(Document.class);
    }

    public BankTransactionMessage parseToInternal(String xml) {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            JAXBElement<Document> root = unmarshaller.unmarshal(new StreamSource(new StringReader(xml)), Document.class);
            Document doc = root.getValue();

            FIToFICustomerCreditTransferV13 fiToFi = doc.getFIToFICstmrCdtTrf();
            if (fiToFi == null) {
                throw new IsoParseException("Missing FIToFICstmrCdtTrf in pacs.008 message");
            }

            List<CreditTransferTransaction70> txList = fiToFi.getCdtTrfTxInf();
            if (txList == null || txList.isEmpty()) {
                throw new IsoParseException("No CdtTrfTxInf elements found in pacs.008 message");
            }

            GroupHeader131 grpHdr = fiToFi.getGrpHdr();
            CreditTransferTransaction70 tx = txList.getFirst();
            return isoPacs008Mapper.toInternal(grpHdr, tx);

        } catch (JAXBException e) {
            throw new IsoParseException("Failed to parse ISO20022 pacs.008 XML");
        }
    }
}
