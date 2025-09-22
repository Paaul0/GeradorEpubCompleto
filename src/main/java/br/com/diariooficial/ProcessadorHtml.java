package br.com.diariooficial;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

public class ProcessadorHtml {

    static final String CAMINHO_ARQUIVO_ENTRADA = "diario.html";
    static final String PASTA_SAIDA_IMAGENS = "tabelas_png_firefox";

    public String executar() throws IOException, InterruptedException {
        System.out.println("Iniciando processo de conversão de tabelas...");
        gerarImagensDasTabelas();
        return substituirTabelasPorImagens();
    }

    private void gerarImagensDasTabelas() throws IOException, InterruptedException {
        System.out.println("\n--- INICIANDO PARTE 1: GERAÇÃO DE IMAGENS ---");

        String htmlContent = Files.readString(Paths.get(CAMINHO_ARQUIVO_ENTRADA), StandardCharsets.UTF_8);
        Files.createDirectories(Paths.get(PASTA_SAIDA_IMAGENS));

        String estiloPadrao = """
        <style>
            body { font-family: Arial, sans-serif; background-color: #ffffff; display: inline-block; padding: 0px; margin: 0; }
            table { border-collapse: collapse; margin: 0; font-size: 1em; min-width: 400px; box-shadow: 0 0 20px rgba(0, 0, 0, 0.15); background-color: #ffffff; }
            th, td { padding: 12px 15px; border: 1px solid #dddddd; text-align: left; }
            thead tr:nth-of-type(even), tbody tr:nth-of-type(even) { background-color: #f3f3f3; }
            tbody tr:last-of-type { border-bottom: 2px solid #0881C8; }
        </style>
        """;

        Document doc = Jsoup.parse(htmlContent);
        Elements tables = doc.select("table");
        System.out.println("Encontradas " + tables.size() + " tabelas. Iniciando conversão com Firefox...");
        System.setProperty("webdriver.gecko.driver", "geckodriver.exe");

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        options.addPreference("layout.css.devPixelsPerPx", "2.0");

        WebDriver driver = new FirefoxDriver(options);
        driver.manage().window().setSize(new Dimension(1920, 1080));

        for (int i = 0; i < tables.size(); i++) {
            System.out.println("--------------------------------------------------");
            System.out.println("Processando Tabela Original #" + (i + 1) + " de " + tables.size());
            Element table = tables.get(i);
            gerarImagensDeTabelaParticionada(table, i + 1, driver, estiloPadrao);
        }
        driver.quit();
        System.out.println("--- Geração de imagens concluída. ---");
    }

    private void gerarImagensDeTabelaParticionada(Element tabelaOriginal, int numeroDaTabela, WebDriver driver, String estilo) throws IOException, InterruptedException {
        final int MAX_LINHAS_POR_IMAGEM = 25;

        Elements todasAsLinhas = tabelaOriginal.select("tr");
        if (todasAsLinhas.isEmpty()) return;

        int totalDeLinhas = todasAsLinhas.size();
        boolean isLonga = totalDeLinhas > MAX_LINHAS_POR_IMAGEM;

        if (isLonga) {
            Elements corpoLinhas = tabelaOriginal.select("tbody tr");
            if (corpoLinhas.isEmpty()) {
                corpoLinhas = new Elements(todasAsLinhas.subList(1, todasAsLinhas.size()));
            }

            int numeroDePartes = (int) Math.ceil((double) corpoLinhas.size() / MAX_LINHAS_POR_IMAGEM);
            System.out.println(">>> Tabela #" + numeroDaTabela + " é LONGA, dividindo por LINHAS em " + numeroDePartes + " partes.");

            Element thead = tabelaOriginal.selectFirst("thead");

            for (int parte = 0; parte < numeroDePartes; parte++) {
                Element tabelaDaParte = tabelaOriginal.clone();
                tabelaDaParte.empty();

                if (thead != null) {
                    tabelaDaParte.appendChild(thead.clone());
                } else {
                    tabelaDaParte.appendChild(todasAsLinhas.get(0).clone());
                }

                Element novoCorpo = tabelaDaParte.appendElement("tbody");
                int linhaInicial = parte * MAX_LINHAS_POR_IMAGEM;
                int linhaFinal = Math.min(linhaInicial + MAX_LINHAS_POR_IMAGEM, corpoLinhas.size());

                for (int i = linhaInicial; i < linhaFinal; i++) {
                    novoCorpo.appendChild(corpoLinhas.get(i).clone());
                }

                String tituloParte = "<h2>Tabela " + numeroDaTabela + " - Parte " + (parte + 1) + " de " + numeroDePartes + "</h2>";
                Path tempFile = Files.createTempFile("table_part_long_", ".html");
                String tempHtmlContent = String.format("<html><head><meta charset='UTF-8'>%s</head><body>%s%s</body></html>", estilo, tituloParte, tabelaDaParte.outerHtml());
                Files.writeString(tempFile, tempHtmlContent);

                driver.get(tempFile.toUri().toString());
                Thread.sleep(300);
                File screenshot = driver.findElement(By.tagName("body")).getScreenshotAs(OutputType.FILE);

                Path destinationFile = Paths.get(PASTA_SAIDA_IMAGENS, "tabela_" + numeroDaTabela + "_parte_" + (parte + 1) + ".png");
                Files.move(screenshot.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(tempFile);
                System.out.println(">>> SUCESSO: Imagem da Parte " + (parte + 1) + " (por linhas) salva.");
            }
        } else {
            System.out.println(">>> Tabela #" + numeroDaTabela + " é pequena, gerando imagem única.");
            Path tempFile = Files.createTempFile("table_single_", ".html");
            String tempHtmlContent = String.format("<html><head><meta charset='UTF-8'>%s</head><body>%s</body></html>", estilo, tabelaOriginal.outerHtml());
            Files.writeString(tempFile, tempHtmlContent);
            driver.get(tempFile.toUri().toString());
            Thread.sleep(300);
            File screenshot = driver.findElement(By.tagName("body")).getScreenshotAs(OutputType.FILE);
            Path destinationFile = Paths.get(PASTA_SAIDA_IMAGENS, "tabela_" + numeroDaTabela + ".png");
            Files.move(screenshot.toPath(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(tempFile);
        }
    }

    private String substituirTabelasPorImagens() throws IOException {
        System.out.println("\n--- INICIANDO SUBSTITUIÇÃO DE TABELAS NO HTML ---");

        String htmlContent = Files.readString(Paths.get(CAMINHO_ARQUIVO_ENTRADA), StandardCharsets.UTF_8);
        Document docParaSubstituicao = Jsoup.parse(htmlContent);
        Elements tabelasParaSubstituir = docParaSubstituicao.select("table");
        docParaSubstituicao.outputSettings().charset(StandardCharsets.UTF_8);

        for (int i = 0; i < tabelasParaSubstituir.size(); i++) {
            Element table = tabelasParaSubstituir.get(i);
            int numeroDaTabela = i + 1;
            StringBuilder imagensHtml = new StringBuilder();

            Path imagemUnicaPath = Paths.get(PASTA_SAIDA_IMAGENS, "tabela_" + numeroDaTabela + ".png");
            if (Files.exists(imagemUnicaPath)) {
                imagensHtml.append(gerarTagDeImagem(imagemUnicaPath));
                System.out.println("Encontrada imagem única para Tabela #" + numeroDaTabela);
            } else {
                System.out.println("Procurando por partes da Tabela #" + numeroDaTabela);
                int parte = 1;
                while (true) {
                    Path imagemDaPartePath = Paths.get(PASTA_SAIDA_IMAGENS, "tabela_" + numeroDaTabela + "_parte_" + parte + ".png");
                    if (Files.exists(imagemDaPartePath)) {
                        imagensHtml.append(gerarTagDeImagem(imagemDaPartePath));
                        System.out.println(" -> Encontrada Parte " + parte);
                        parte++;
                    } else {
                        break;
                    }
                }
            }

            if (imagensHtml.length() > 0) {
                table.before(imagensHtml.toString());
                table.remove();
                System.out.println("Substituindo Tabela #" + numeroDaTabela + " por uma ou mais imagens.");
            } else {
                System.out.println("AVISO: Nenhuma imagem (única ou em partes) foi encontrada para a Tabela #" + numeroDaTabela + ". A tabela original será mantida.");
            }
        }
        System.out.println("--- Substituição de tabelas concluída. ---");
        return docParaSubstituicao.outerHtml();
    }

    private String gerarTagDeImagem(Path imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imagePath);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String imgSrc = "data:image/png;base64," + base64Image;
        return "<div style='margin-bottom: 15px;'>"
                + "<img src='" + imgSrc + "' alt='" + imagePath.getFileName() + "' style='max-width: 100%; height: auto;' />"
                + "</div>";
    }
}