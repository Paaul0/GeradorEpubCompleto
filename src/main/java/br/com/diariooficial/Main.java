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

        byte[] imagemCapaPrincipal = null;

        try {
            System.out.println("\n--- INICIANDO PRÉ-PROCESSAMENTO: Garantindo codificação UTF-8 ---");
            garantirArquivoEmUtf8();
            System.out.println("--- PRÉ-PROCESSAMENTO CONCLUÍDO ---\n");

            System.out.println("--- INICIANDO ETAPA 0: Geração da Capa Dinâmica a partir do Base64 ---");
            imagemCapaPrincipal = gerarCapaDinamica();
            System.out.println("--- ETAPA 0 CONCLUÍDA ---\n");

            System.out.println("--- INICIANDO ETAPA 1: Processamento do HTML e Tabelas ---");
            ProcessadorHtml processador = new ProcessadorHtml();
            String htmlComImagens = processador.executar();
            System.out.println("--- ETAPA 1 CONCLUÍDA ---\n");

            // --- NOVO CÓDIGO ADICIONADO AQUI ---
            System.out.println("--- INICIANDO ETAPA 1.5: Salvando o HTML Processado ---");
            Path caminhoSaidaHtml = Paths.get("diario_final.html");
            Files.writeString(caminhoSaidaHtml, htmlComImagens, StandardCharsets.UTF_8);
            System.out.println(">>> SUCESSO: O HTML processado foi salvo em: " + caminhoSaidaHtml.toAbsolutePath());
            System.out.println("--- ETAPA 1.5 CONCLUÍDA ---\n");
            // --- FIM DO NOVO CÓDIGO ---

            System.out.println("--- INICIANDO ETAPA 2: Geração do Arquivo EPUB ---");
            ConversorEpub conversor = new ConversorEpub();
            conversor.gerar(htmlComImagens, imagemCapaPrincipal);
            System.out.println("--- ETAPA 2 CONCLUÍDA ---\n");

            System.out.println("### PROCESSO FINALIZADO COM SUCESSO! ###");

        } catch (Exception e) {
            System.err.println("Ocorreu um erro durante o processo:");
            e.printStackTrace();
        }
    }

    private static void garantirArquivoEmUtf8() throws IOException {
        Path caminhoArquivo = Paths.get(ProcessadorHtml.CAMINHO_ARQUIVO_ENTRADA);
        if (!Files.exists(caminhoArquivo)) {
            throw new IOException("Arquivo de entrada não encontrado em: " + caminhoArquivo.toAbsolutePath());
        }
        String conteudo = Files.readString(caminhoArquivo, Charset.forName("ISO-8859-1"));
        Files.writeString(caminhoArquivo, conteudo, StandardCharsets.UTF_8);
        System.out.println("Arquivo de entrada convertido para UTF-8 com sucesso.");
    }

    private static byte[] gerarCapaDinamica() {
        GeradorImagem servicoGeradorImagem = new GeradorImagem();
        LocalDateTime agora = LocalDateTime.now();
        Locale localBrasil = new Locale("pt", "BR");

        String dia = agora.format(DateTimeFormatter.ofPattern("dd"));
        String mesPorExtenso = agora.format(DateTimeFormatter.ofPattern("MMMM", localBrasil));
        String ano = agora.format(DateTimeFormatter.ofPattern("yyyy"));
        int edicao = 186;
        int anoDiario = 70;

        String textoCompleto = String.format(
                "São Paulo, %s de %s de %s | Edição %d | Ano %d",
                dia, mesPorExtenso, ano, edicao, anoDiario
        );
        System.out.println("Gerando imagem de capa com o texto: " + textoCompleto);

        return servicoGeradorImagem.gerarImagem(textoCompleto);
    }
}