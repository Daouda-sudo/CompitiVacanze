import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ElaboratoreDatiCsv {

    private final String percorsoFile;

    public ElaboratoreDatiCsv(String percorsoFile) {
        this.percorsoFile = percorsoFile;
    }

    public RisultatiElaborazione elabora() throws IOException {
        int totaleIndividui = 0;
        Map<String, Integer> conteggioPerSpecie = new HashMap<>();
        Set<String> localitaUniche = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(percorsoFile))) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String[] campi = linea.split(",", -1);
                if (campi.length < 28) {
                    throw new IOException("Riga non conforme");
                }

                String nomeScientifico = campi[5];
                String localita = campi[0];
                int conteggio = "NA".equals(campi[13]) ? 0 : Integer.parseInt(campi[13]);

                totaleIndividui += conteggio;
                conteggioPerSpecie.merge(nomeScientifico, conteggio, Integer::sum);
                localitaUniche.add(localita);
            }
        }

        String speciePiuAbbondante = null;
        int max = -1;
        for (Map.Entry<String, Integer> entry : conteggioPerSpecie.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                speciePiuAbbondante = entry.getKey();
            }
        }

        return new RisultatiElaborazione(totaleIndividui, conteggioPerSpecie, speciePiuAbbondante, localitaUniche);
    }

    public static class RisultatiElaborazione {
        private final int totaleIndividui;
        private final Map<String, Integer> conteggioPerSpecie;
        private final String speciePiuAbbondante;
        private final Set<String> localitaUniche;

        public RisultatiElaborazione(int totaleIndividui, Map<String, Integer> conteggioPerSpecie,
                                     String speciePiuAbbondante, Set<String> localitaUniche) {
            this.totaleIndividui = totaleIndividui;
            this.conteggioPerSpecie = new HashMap<>(conteggioPerSpecie);
            this.speciePiuAbbondante = speciePiuAbbondante;
            this.localitaUniche = new HashSet<>(localitaUniche);
        }

        public int getTotaleIndividui() {
            return totaleIndividui;
        }

        public Map<String, Integer> getConteggioPerSpecie() {
            return new HashMap<>(conteggioPerSpecie);
        }

        public String getSpeciePiuAbbondante() {
            return speciePiuAbbondante;
        }

        public Set<String> getLocalitaUniche() {
            return new HashSet<>(localitaUniche);
        }
    }
}
