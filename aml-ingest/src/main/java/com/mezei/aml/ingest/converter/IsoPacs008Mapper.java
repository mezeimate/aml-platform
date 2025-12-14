package com.mezei.aml.ingest.converter;

import com.mezei.aml.ingest.transaction.dto.BankTransactionMessage;
import iso.std.iso._20022.tech.xsd.pacs_008_001.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;

@Slf4j
@Component
public class IsoPacs008Mapper {

    public BankTransactionMessage toInternal(GroupHeader131 grpHdr, CreditTransferTransaction70 tx) {
        String endToEndId = tx.getPmtId().getEndToEndId();
        Instant creDtTm = toInstant(grpHdr.getCreDtTm());
        String debtorIban = tx.getDbtrAcct().getId().getIBAN();
        String cdtrIban = tx.getCdtrAcct().getId().getIBAN();
        String nm = tx.getCdtr().getNm();
        ActiveCurrencyAndAmount intrBkSttlmAmt = tx.getIntrBkSttlmAmt();

        log.info("Received ISO20022 pacs.008 MsgId={} EndToEndId={}", grpHdr.getMsgId(), endToEndId);

        BankTransactionMessage msg = new BankTransactionMessage(
                endToEndId,
                creDtTm,
                "ISO20022_PACS008",
                debtorIban,
                debtorIban,
                cdtrIban,
                nm,
                intrBkSttlmAmt.getValue(),
                intrBkSttlmAmt.getCcy(),
                "DEBIT",
                debtorIban.substring(0, 2),
                debtorIban.substring(0, 2),
                cdtrIban.substring(0, 2),
                null,
                tx.getRmtInf().getUstrd().getFirst()
        );

        return msg;
    }

    private Instant toInstant(XMLGregorianCalendar xmlCal) {
        if (xmlCal == null) {
            return null;
        }
        return xmlCal.toGregorianCalendar().toInstant();
    }


}
