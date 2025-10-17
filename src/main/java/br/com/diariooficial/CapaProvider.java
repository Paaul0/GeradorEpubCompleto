package br.com.diariooficial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CapaProvider {

    // Carrega o template e a fonte uma vez e os mantém em memória.
    private static final String CAPA_PRINCIPAL_TEMPLATE_BASE64 = carregarResourceComoString("/template_base64.txt");
    private static final String FONTE_OPENSANS_BASE64 = carregarResourceComoString("/OpenSans-Regular.ttf_base64.txt");

    // Mapeia a palavra-chave da seção para o NOME do seu arquivo de texto correspondente.
    private static final Map<String, String> MAPA_ARQUIVOS_CAPAS_SECAO;

    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("atos do executivo", "atos_executivo.txt");
        aMap.put("concursos", "concursos.txt");
        aMap.put("editais", "editais.txt");
        aMap.put("negócios", "negocios.txt");
        aMap.put("servidores", "servidores.txt");
        aMap.put("atos da cmsp", "atos_cmsp.txt");
        aMap.put("atos do tcm-sp", "atos_tcm.txt");
        MAPA_ARQUIVOS_CAPAS_SECAO = Collections.unmodifiableMap(aMap);
    }

    /**
     * Retorna a string Base64 do template da capa principal.
     */
    public static String getCapaPrincipalTemplateBase64() {
        return CAPA_PRINCIPAL_TEMPLATE_BASE64;
    }

    /**
     * Retorna a string Base64 da fonte.
     */
    public static String getFonteBase64() {
        return FONTE_OPENSANS_BASE64;
    }

    /**
     * Retorna o mapa que associa palavras-chave aos nomes dos arquivos de capa.
     */
    public static Map<String, String> getMapaDeCapas() {
        return MAPA_ARQUIVOS_CAPAS_SECAO;
    }

    /**
     * Carrega o conteúdo Base64 de um arquivo de capa de seção específico.
     * @param nomeArquivo O nome do arquivo (ex: "secao_concursos.txt")
     * @return A string Base64 contida no arquivo.
     */
    public static String getCapaSecaoBase64(String nomeArquivo) {
        // Assume que os arquivos estão numa subpasta "base64" dentro de "resources"
        // Se não estiverem, remova o "/base64" daqui.
        return carregarResourceComoString("/" + nomeArquivo);
    }

    /**
     * Método auxiliar que lê um arquivo da pasta resources e retorna seu conteúdo como String.
     */
    private static String carregarResourceComoString(String caminho) {
        try (InputStream is = CapaProvider.class.getResourceAsStream(caminho)) {
            if (is == null) {
                throw new IOException("Recurso não encontrado: " + caminho);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                // Remove quebras de linha que editores de texto podem inserir
                return reader.lines().collect(Collectors.joining("")).trim();
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar recurso: " + caminho, e);
        }
    }
}