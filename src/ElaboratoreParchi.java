import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ElaboratoreParchi {

    private final String percorsoFile;
    private List<Record> records;
    private List<String> header;

    public ElaboratoreParchi(String percorsoFile) {
        this.percorsoFile = percorsoFile;
        this.records = new ArrayList<>();
        this.header = Arrays.asList(
                "Borough Location", "Park Location", "Sports Played", "Week Start Date", "Week End Date",
                "Sunday's Attendance", "Monday's Attendance", "Tuesday's Attendance", "Wednesday's Attendance",
                "Thursday's Attendance", "Friday's Attendance", "Saturday's Attendance", "Attendance Sum",
                "miovalore", "cancellato"
        );
    }

    public void caricaDati() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(percorsoFile))) {
            String line = reader.readLine();
            if (line == null) throw new IOException("File vuoto");

            Random rand = new Random();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] fields = parseCsvLine(line);
                if (fields.length < 13) continue;

                String[] extended = new String[15];
                System.arraycopy(fields, 0, extended, 0, 13);
                extended[13] = String.valueOf(10 + rand.nextInt(11));
                extended[14] = "false";

                records.add(new Record(extended));
            }
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    public int getNumeroCampi() {
        return header.size();
    }

    public int getLunghezzaMassimaRecord() {
        int max = 0;
        for (Record r : records) {
            if ("false".equals(r.cancellato)) {
                int len = String.join(" ", r.getCampi()).length();
                if (len > max) max = len;
            }
        }
        return max;
    }

    public List<Integer> getLunghezzaMassimaPerCampo() {
        List<Integer> maxLen = new ArrayList<>(Collections.nCopies(header.size(), 0));
        for (Record r : records) {
            if ("false".equals(r.cancellato)) {
                String[] campi = r.getCampi();
                for (int i = 0; i < campi.length; i++) {
                    int len = campi[i].length();
                    if (len > maxLen.get(i)) {
                        maxLen.set(i, len);
                    }
                }
            }
        }
        return maxLen;
    }

    public List<String> getAllineamentoAFissaLarghezza() {
        List<Integer> maxLen = getLunghezzaMassimaPerCampo();
        List<String> risultato = new ArrayList<>();
        risultato.add(formattaRigaFissa(header, maxLen));
        for (Record r : records) {
            if ("false".equals(r.cancellato)) {
                risultato.add(formattaRigaFissa(Arrays.asList(r.getCampi()), maxLen));
            }
        }
        return risultato;
    }

    private String formattaRigaFissa(List<String> campi, List<Integer> larghezze) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campi.size(); i++) {
            if (i > 0) sb.append(" ");
            String valore = campi.get(i);
            int larghezza = larghezze.get(i);
            sb.append(String.format("%-" + larghezza + "s", valore));
        }
        return sb.toString();
    }

    public void aggiungiRecord() {
        String[] nuovi = new String[15];
        Arrays.fill(nuovi, "NUOVO");
        nuovi[13] = String.valueOf(10 + new Random().nextInt(11));
        nuovi[14] = "false";
        records.add(new Record(nuovi));
    }

    public void visualizzaTreCampiSignificativi() {
        for (Record r : records) {
            if ("false".equals(r.cancellato)) {
                System.out.println(r.boroughLocation + " | " + r.parkLocation + " | " + r.attendanceSum);
            }
        }
    }

    public Record cercaRecord(String chiave) {
        for (Record r : records) {
            if ("false".equals(r.cancellato) &&
                    (r.boroughLocation + "_" + r.parkLocation + "_" + r.weekStartDate).equals(chiave)) {
                return r;
            }
        }
        return null;
    }

    public boolean modificaRecord(String chiave, String nuovoValore) {
        Record r = cercaRecord(chiave);
        if (r == null) return false;
        r.attendanceSum = nuovoValore;
        return true;
    }

    public boolean cancellaLogicamente(String chiave) {
        Record r = cercaRecord(chiave);
        if (r == null) return false;
        r.cancellato = "true";
        return true;
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

        aggiungiRecord();

        System.out.println("\nTre campi significativi (dopo aggiunta):");
        visualizzaTreCampiSignificativi();

        if (!records.isEmpty()) {
            Record primo = records.get(0);
            String chiave = primo.boroughLocation + "_" + primo.parkLocation + "_" + primo.weekStartDate;
            System.out.println("\nChiave usata: " + chiave);

            System.out.println("Modifica Attendance Sum a 9999");
            modificaRecord(chiave, "9999");

            System.out.println("Cancellazione logica del secondo record");
            if (records.size() > 1) {
                Record secondo = records.get(1);
                String chiave2 = secondo.boroughLocation + "_" + secondo.parkLocation + "_" + secondo.weekStartDate;
                cancellaLogicamente(chiave2);
            }

            System.out.println("\nDopo modifiche:");
            visualizzaTreCampiSignificativi();
        }
    }

    private static class Record {
        String boroughLocation;
        String parkLocation;
        String sportsPlayed;
        String weekStartDate;
        String weekEndDate;
        String sundayAttendance;
        String mondayAttendance;
        String tuesdayAttendance;
        String wednesdayAttendance;
        String thursdayAttendance;
        String fridayAttendance;
        String saturdayAttendance;
        String attendanceSum;
        String miovalore;
        String cancellato;

        public Record(String[] campi) {
            boroughLocation = campi[0];
            parkLocation = campi[1];
            sportsPlayed = campi[2];
            weekStartDate = campi[3];
            weekEndDate = campi[4];
            sundayAttendance = campi[5];
            mondayAttendance = campi[6];
            tuesdayAttendance = campi[7];
            wednesdayAttendance = campi[8];
            thursdayAttendance = campi[9];
            fridayAttendance = campi[10];
            saturdayAttendance = campi[11];
            attendanceSum = campi[12];
            miovalore = campi[13];
            cancellato = campi[14];
        }

        public String[] getCampi() {
            return new String[]{
                    boroughLocation, parkLocation, sportsPlayed, weekStartDate, weekEndDate,
                    sundayAttendance, mondayAttendance, tuesdayAttendance, wednesdayAttendance,
                    thursdayAttendance, fridayAttendance, saturdayAttendance, attendanceSum,
                    miovalore, cancellato
            };
        }
    }
}
