package br.com.diariooficial;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        System.out.println("### PROCESSO UNIFICADO DE GERAÇÃO DE EPUB INICIADO ###");

        try {
            // ================== ETAPA DE PRÉ-PROCESSAMENTO ==================
            System.out.println("\n--- INICIANDO PRÉ-PROCESSAMENTO: Garantindo codificação UTF-8 ---");
            garantirArquivoEmUtf8();
            System.out.println("--- PRÉ-PROCESSAMENTO CONCLUÍDO ---\n");

            // ================== ETAPA 0: GERAR CAPA DINÂMICA ==================
            System.out.println("--- INICIANDO ETAPA 0: Geração da Capa Dinâmica ---");
            gerarCapaDinamica();
            System.out.println("--- ETAPA 0 CONCLUÍDA ---\n");

            // ================== ETAPA 1: PROCESSAR O HTML ==================
            System.out.println("--- INICIANDO ETAPA 1: Processamento do HTML e Tabelas ---");
            ProcessadorHtml processador = new ProcessadorHtml();
            String htmlComImagens = processador.executar();
            System.out.println("--- ETAPA 1 CONCLUÍDA ---\n");

            // ================== ETAPA 2: GERAR O EPUB ==================
            System.out.println("--- INICIANDO ETAPA 2: Geração do Arquivo EPUB ---");
            ConversorEpub conversor = new ConversorEpub();
            conversor.gerar(htmlComImagens);
            System.out.println("--- ETAPA 2 CONCLUÍDA ---\n");

            System.out.println("### PROCESSO FINALIZADO COM SUCESSO! ###");

        } catch (Exception e) {
            System.err.println("Ocorreu um erro durante o processo:");
            e.printStackTrace();
        }
    }

    /**
     * NOVO MÉTODO: Lê o arquivo de entrada com a codificação ISO-8859-1
     * e o reescreve imediatamente como UTF-8.
     */
    private static void garantirArquivoEmUtf8() throws IOException {
        Path caminhoArquivo = Paths.get(ProcessadorHtml.CAMINHO_ARQUIVO_ENTRADA);

        if (!Files.exists(caminhoArquivo)) {
            throw new IOException("Arquivo de entrada não encontrado em: " + caminhoArquivo.toAbsolutePath());
        }

        // Lê o conteúdo usando a codificação ISO-8859-1
        String conteudo = Files.readString(caminhoArquivo, Charset.forName("ISO-8859-1"));

        // Reescreve o mesmo arquivo, mas agora forçando a codificação para UTF-8
        Files.writeString(caminhoArquivo, conteudo, StandardCharsets.UTF_8);

        System.out.println("Arquivo de entrada convertido para UTF-8 com sucesso.");
    }

    /**
     * Este método contém a lógica do seu terceiro projeto.
     * Ele gera a imagem da capa e a salva diretamente em 'Capas/1.png'.
     */
    private static void gerarCapaDinamica() throws IOException {
        GeradorImagem servicoGeradorImagem = new GeradorImagem();
        LocalDateTime agora = LocalDateTime.now();
        Locale localBrasil = new Locale("pt", "BR");

        String dia = agora.format(DateTimeFormatter.ofPattern("dd"));
        String mesPorExtenso = agora.format(DateTimeFormatter.ofPattern("MMMM", localBrasil));
        String ano = agora.format(DateTimeFormatter.ofPattern("yyyy"));
        int edicao = 186; // Você pode tornar isso dinâmico se precisar
        int anoDiario = 70;

        String textoCompleto = String.format(
                "São Paulo, %s de %s de %s | Edição %d | Ano %d",
                dia, mesPorExtenso, ano, edicao, anoDiario
        );

        DadosImagem dadosParaImagem = new DadosImagem(textoCompleto, "", "", "");
        System.out.println("Gerando imagem de capa com o texto: " + textoCompleto);

        byte[] imagemBytes = servicoGeradorImagem.gerarImagem(dadosParaImagem);

        Path pastaCapas = Paths.get("Capas");
        if (!Files.exists(pastaCapas)) {
            Files.createDirectories(pastaCapas);
        }

        Path caminhoDeSaida = pastaCapas.resolve("1.png");
        Files.write(caminhoDeSaida, imagemBytes);

        System.out.println("Capa dinâmica gerada e salva com sucesso em: " + caminhoDeSaida.toAbsolutePath());
    }
}