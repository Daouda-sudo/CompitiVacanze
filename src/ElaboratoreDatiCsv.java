import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ElaboratoreDatiCsv {

    private final String percorsoFile;
    private List<Record> records;
    private List<String> header;

    public ElaboratoreDatiCsv(String percorsoFile) {
        this.percorsoFile = percorsoFile;
        this.records = new ArrayList<>();
        this.header = new ArrayList<>();
    }

    public void caricaDati() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(percorsoFile))) {
            String line = reader.readLine();
            if (line == null) throw new IOException("File vuoto");
            String[] hdr = line.split(",", -1);
            header = Arrays.asList(hdr);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] fields = line.split(",", -1);
                records.add(new Record(fields));
            }
        }
    }

    public int getNumeroCampi() {
        return header.size();
    }

    public int getLunghezzaMassimaRecord() {
        int max = 0;
        for (Record r : records) {
            int len = String.join(",", r.getCampi()).length();
            if (len > max) max = len;
        }
        return max;
    }

    public List<Integer> getLunghezzaMassimaPerCampo() {
        List<Integer> maxPerCol = new ArrayList<>(Collections.nCopies(header.size(), 0));
        for (Record r : records) {
            String[] campi = r.getCampi();
            for (int i = 0; i < campi.length && i < header.size(); i++) {
                int len = campi[i].length();
                if (len > maxPerCol.get(i)) {
                    maxPerCol.set(i, len);
                }
            }
        }
        return maxPerCol;
    }

    public List<String> getAllineamentoAFissaLarghezza() {
        List<Integer> maxLen = getLunghezzaMassimaPerCampo();
        List<String> risultato = new ArrayList<>();
        risultato.add(formattaRigaFissa(header, maxLen));
        for (Record r : records) {
            if (!r.isDeleted()) {
                risultato.add(formattaRigaFissa(Arrays.asList(r.getCampi()), maxLen));
            }
        }
        return risultato;
    }

    private String formattaRigaFissa(List<String> campi, List<Integer> larghezze) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campi.size(); i++) {
            if (i > 0) sb.append(" ");
            String valore = i < campi.size() ? campi.get(i) : "";
            int larghezza = i < larghezze.size() ? larghezze.get(i) : valore.length();
            sb.append(String.format("%-" + larghezza + "s", valore));
        }
        return sb.toString();
    }

    public void aggiungiRecord(String[] nuoviCampi) {
        if (nuoviCampi.length != header.size()) {
            throw new IllegalArgumentException("Numero di campi non corrispondente");
        }
        records.add(new Record(nuoviCampi));
    }

    public void visualizzaTreCampiSignificativi() {
        for (Record r : records) {
            if (!r.isDeleted()) {
                System.out.println(r.locality + " | " + r.scientificName + " | " + r.individualCount);
            }
        }
    }

    public Record cercaRecord(String chiave) {
        for (Record r : records) {
            if (!r.isDeleted() && (r.locality + "_" + r.scientificName + "_" + r.eventDate).equals(chiave)) {
                return r;
            }
        }
        return null;
    }





    public void eseguiTutteOperazioni() throws IOException {
        caricaDati();

        System.out.println("Numero campi: " + getNumeroCampi());

        System.out.println("Lunghezza massima record: " + getLunghezzaMassimaRecord());

        List<Integer> maxPerCampo = getLunghezzaMassimaPerCampo();
        System.out.println("Lunghezza massima per campo: " + maxPerCampo);

        List<String> allineati = getAllineamentoAFissaLarghezza();
        System.out.println("Primi 2 record allineati:");
        for (int i = 0; i < Math.min(3, allineati.size()); i++) {
            System.out.println(allineati.get(i));
        }

        aggiungiRecord(new String[]{"NUOVA", "Event", "HumanObservation", "2025-01-01", "A", "Specie nuova", "A", "species", "Specie nuova", "unknown", "Note", "Autore", "5", "R", "NA", "0°0'0\" N 0°0'0\" W", "DMS", "NAD83", "999", "Nuova località", "Note campo", "Contea", "Stato", "NA", "FALSE", "NA", "NA", "NA"});

        System.out.println("\nTre campi significativi:");
        visualizzaTreCampiSignificativi();

        String chiave = records.get(0).locality + "_" + records.get(0).scientificName + "_" + records.get(0).eventDate;
        System.out.println("\nRicerca record con chiave: " + chiave);
        if (cercaRecord(chiave) != null) {
            System.out.println("Record trovato.");
        }




        System.out.println("\nDopo modifiche:");
        visualizzaTreCampiSignificativi();
    }

    private static class Record {
        String locality;
        String type;
        String basisOfRecord;
        String eventDate;
        String eventDate_flag;
        String scientificName;
        String scientificName_flag;
        String taxonRank;
        String verbatimIdentification;
        String namePublishedIn;
        String custom_TaxonomicNotes;
        String recordedBy;
        String individualCount;
        String coordinate_flag;
        String geodeticDatum;
        String verbatimCoordinates;
        String verbatimCoordinateSystem;
        String verbatimSRS;
        String locationID;
        String locationRemarks;
        String fieldNotes;
        String county;
        String stateProvince;
        String establishmentMeans;
        String custom_SensitiveRecord;
        String informationWithheld;
        String dataGeneralizations;
        String footprintWKT;
        boolean deleted = false;

        private static final List<String> NOMI_CAMPI = Arrays.asList(
                "locality", "type", "basisOfRecord", "eventDate", "eventDate_flag", "scientificName",
                "scientificName_flag", "taxonRank", "verbatimIdentification", "namePublishedIn",
                "custom_TaxonomicNotes", "recordedBy", "individualCount", "coordinate_flag",
                "geodeticDatum", "verbatimCoordinates", "verbatimCoordinateSystem", "verbatimSRS",
                "locationID", "locationRemarks", "fieldNotes", "county", "stateProvince",
                "establishmentMeans", "custom_SensitiveRecord", "informationWithheld",
                "dataGeneralizations", "footprintWKT"
        );

        public Record(String[] campi) {
            if (campi.length >= 28) {
                this.locality = campi[0];
                this.type = campi[1];
                this.basisOfRecord = campi[2];
                this.eventDate = campi[3];
                this.eventDate_flag = campi[4];
                this.scientificName = campi[5];
                this.scientificName_flag = campi[6];
                this.taxonRank = campi[7];
                this.verbatimIdentification = campi[8];
                this.namePublishedIn = campi[9];
                this.custom_TaxonomicNotes = campi[10];
                this.recordedBy = campi[11];
                this.individualCount = campi[12];
                this.coordinate_flag = campi[13];
                this.geodeticDatum = campi[14];
                this.verbatimCoordinates = campi[15];
                this.verbatimCoordinateSystem = campi[16];
                this.verbatimSRS = campi[17];
                this.locationID = campi[18];
                this.locationRemarks = campi[19];
                this.fieldNotes = campi[20];
                this.county = campi[21];
                this.stateProvince = campi[22];
                this.establishmentMeans = campi[23];
                this.custom_SensitiveRecord = campi[24];
                this.informationWithheld = campi[25];
                this.dataGeneralizations = campi[26];
                this.footprintWKT = campi[27];
            }
        }

        public String[] getCampi() {
            return new String[]{
                    locality, type, basisOfRecord, eventDate, eventDate_flag, scientificName,
                    scientificName_flag, taxonRank, verbatimIdentification, namePublishedIn,
                    custom_TaxonomicNotes, recordedBy, individualCount, coordinate_flag,
                    geodeticDatum, verbatimCoordinates, verbatimCoordinateSystem, verbatimSRS,
                    locationID, locationRemarks, fieldNotes, county, stateProvince,
                    establishmentMeans, custom_SensitiveRecord, informationWithheld,
                    dataGeneralizations, footprintWKT
            };
        }

        public boolean impostaCampo(String nomeCampo, String valore) {
            switch (nomeCampo) {
                case "locality": locality = valore; break;
                case "scientificName": scientificName = valore; break;
                case "individualCount": individualCount = valore; break;
                case "eventDate": eventDate = valore; break;
                default: return false;
            }
            return true;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }
    }
}