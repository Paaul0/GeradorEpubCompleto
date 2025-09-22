package br.com.diariooficial;

import nl.siegmann.epublib.domain.*;
import nl.siegmann.epublib.epub.EpubWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConversorEpub {


    public void gerar(String htmlProcessado) throws IOException {
        Map<String, String> mapaDeCapas = new HashMap<>();
        mapaDeCapas.put("atos do executivo", "2.png");
        mapaDeCapas.put("concursos", "3.png");
        mapaDeCapas.put("editais", "4.png");
        mapaDeCapas.put("negócios", "5.png");
        mapaDeCapas.put("servidores", "6.png");
        mapaDeCapas.put("atos da cmsp", "7.png");
        mapaDeCapas.put("atos do tcm-sp", "8.png");

        Book livro = new Book();
        Metadata metadata = livro.getMetadata();
        metadata.getIdentifiers().add(new Identifier(Identifier.Scheme.UUID, UUID.randomUUID().toString()));
        metadata.addTitle("Diário Oficial de São Paulo");
        metadata.addAuthor(new Author("Prefeitura", "de São Paulo"));
        metadata.getPublishers().add("SEGES/ARQUIP");
        metadata.getDescriptions().add("Edição digital do Diário Oficial de São Paulo ");
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        metadata.getDates().add(new Date(java.util.Date.from(hoje.atStartOfDay(ZoneId.systemDefault()).toInstant()), Date.Event.PUBLICATION));
        metadata.setLanguage("pt-BR");

        File arquivoCapaPrincipal = new File("Capas/1.png");
        if (arquivoCapaPrincipal.exists()) {
            Resource coverResource = new Resource(new FileInputStream(arquivoCapaPrincipal), "cover.png");
            livro.setCoverImage(coverResource);
            System.out.println("Capa principal '1.png' adicionada aos metadados.");

            String coverPageContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                    "<head><title>Capa</title><style type=\"text/css\"> body { margin: 0; padding: 0; text-align: center; } img { max-width: 100%; height: 100vh; object-fit: contain; } </style></head>" +
                    "<body>" +
                    "<img src=\"" + coverResource.getHref() + "\" alt=\"Capa do Livro\"/>" +
                    "</body>" +
                    "</html>";
            Resource coverPageResource = new Resource(coverPageContent.getBytes(StandardCharsets.UTF_8), "cover.xhtml");
            livro.addSection("Capa Principal", coverPageResource);
            System.out.println("Página de capa principal (cover.xhtml) adicionada.");
        } else {
            System.out.println("\n Aviso: Arquivo da capa principal 'Capas/1.png' não encontrado.");
        }

        System.out.println("\n--- Criando Página de Editorial ---");
        GeradorPaginaEditorial geradorEditorial = new GeradorPaginaEditorial(LocalDate.now());
        String conteudoEditorial = geradorEditorial.gerarHtml();
        Resource editorialResource = new Resource(conteudoEditorial.getBytes(StandardCharsets.UTF_8), "editorial.xhtml");
        livro.addResource(editorialResource);
        livro.getSpine().addResource(editorialResource);
        System.out.println("Página de editorial adicionada com sucesso.");

        Document doc = Jsoup.parse(htmlProcessado);

        Safelist safelist = Safelist.relaxed()
                .addTags("figure", "span", "table", "tbody", "tr", "td", "div", "ul", "li", "a")
                .addAttributes(":all", "style", "class", "id")
                .addAttributes("a", "href")
                .addAttributes("img", "src")
                .addProtocols("img", "src", "data");
        doc = new Cleaner(safelist).clean(doc);
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml).charset(StandardCharsets.UTF_8);

        Resource cssResource = null;
        Element styleTag = doc.selectFirst("style");
        if (styleTag != null) {
            cssResource = new Resource(styleTag.html().getBytes(StandardCharsets.UTF_8), "estilos.css");
            livro.addResource(cssResource);
            System.out.println("CSS interno extraído com sucesso.");
        }

        Elements allHeadings = doc.select("h1, h2");
        int headingIndex = 0;
        for (Element heading : allHeadings) {
            String id = normalizarNome(heading.text()) + "-" + (headingIndex++);
            heading.attr("id", id);
        }

        Elements sectionsH1 = doc.select("h1");
        TableOfContents toc = livro.getTableOfContents();
        int sectionCounter = 1;

        System.out.println("Iniciando processamento de " + sectionsH1.size() + " seções...");

        for (Element h1 : sectionsH1) {
            System.out.println("\n--- Processando Seção " + sectionCounter + ": " + h1.text() + " ---");

            Resource capaSecaoPageResource = null;
            String h1TextoNormalizado = h1.text().toLowerCase();
            String nomeArquivoCapa = null;

            for (Map.Entry<String, String> entry : mapaDeCapas.entrySet()) {
                if (h1TextoNormalizado.contains(entry.getKey())) {
                    nomeArquivoCapa = entry.getValue();
                    break;
                }
            }

            if (nomeArquivoCapa != null) {
                File arquivoCapaSecao = new File("Capas/" + nomeArquivoCapa);

                if (arquivoCapaSecao.exists()) {
                    String epubCapaHref = "capa_secao_" + sectionCounter + ".png";
                    Resource capaSecaoResource = new Resource(new FileInputStream(arquivoCapaSecao), epubCapaHref);
                    livro.addResource(capaSecaoResource);

                    String capaSecaoPageContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">" +
                            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                            "<head><title>Capa da Seção</title><style type=\"text/css\"> body { margin: 0; padding: 0; text-align: center; } img { max-width: 100%; height: 100vh; object-fit: contain; } </style></head>" +
                            "<body><img src=\"" + epubCapaHref + "\" alt=\"Capa da Seção\"/></body></html>";

                    capaSecaoPageResource = new Resource(capaSecaoPageContent.getBytes(StandardCharsets.UTF_8), "pagina_capa_secao_" + sectionCounter + ".xhtml");
                    livro.addResource(capaSecaoPageResource);
                    livro.getSpine().addResource(capaSecaoPageResource);
                    System.out.println("Capa '" + nomeArquivoCapa + "' adicionada para a seção '" + h1.text() + "'.");
                } else {
                    System.out.println("Aviso: Capa '" + nomeArquivoCapa + "' mapeada, mas arquivo não encontrado em 'Capas/'.");
                }
            } else {
                System.out.println("Aviso: Nenhuma capa mapeada para a seção '" + h1.text() + "'.");
            }

            StringBuilder sectionHtmlBuilder = new StringBuilder();
            sectionHtmlBuilder.append(h1.outerHtml());
            Element nextElement = h1.nextElementSibling();
            while (nextElement != null && !nextElement.tagName().equalsIgnoreCase("h1")) {
                sectionHtmlBuilder.append(nextElement.outerHtml());
                nextElement = nextElement.nextElementSibling();
            }

            Document sectionDoc = Jsoup.parseBodyFragment(sectionHtmlBuilder.toString());
            Elements h2sInSection = sectionDoc.select("h2");
            sectionDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            if (!h2sInSection.isEmpty()) {
                System.out.println("Gerando sumário interno para " + h2sInSection.size() + " subtítulos (H2).");
                StringBuilder miniTocHtml = new StringBuilder();
                miniTocHtml.append("<div class=\"sumario-secao\">")
                        .append("<p class=\"sumario-titulo\">Sumário da seção:</p>")
                        .append("<ul>\n");

                for (Element h2 : h2sInSection) {
                    miniTocHtml.append("<li><a href=\"#").append(h2.id()).append("\">")
                            .append(h2.text()).append("</a></li>\n");
                }
                miniTocHtml.append("</ul></div>");

                Element h1InSec = sectionDoc.selectFirst("h1");
                if (h1InSec != null) {
                    h1InSec.after(miniTocHtml.toString());
                }
            }

            StringBuilder xhtmlContent = new StringBuilder();
            xhtmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                    .append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n")
                    .append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>")
                    .append("<title>").append(h1.text()).append("</title>");
            if (cssResource != null) {
                xhtmlContent.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(cssResource.getHref()).append("\" />");
            }
            xhtmlContent.append("<style>")
                    .append(".sumario-secao { column-count: 2; column-rule: 1px solid #434444; background-color: #f5f5f5; padding: 20px; font-size: 12px; border-radius: 8px; border: 1px solid #e0e0e0; margin: 25px 0; }")
                    .append(".sumario-secao ul { list-style-type: none; padding-left: 0; margin: 0; }")
                    .append(".sumario-titulo { font-weight: bold; font-size: 1.1em; margin-top: 0; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #d0d0d0; column-span: all; }")
                    .append(".sumario-secao li { padding-bottom: 12px; }")
                    .append(".sumario-secao li a { text-decoration: none; color: #424242; font-size: 0.95em; }")
                    .append(".sumario-secao li a:hover { color: #000000; text-decoration: underline; }")
                    .append("</style>");
            xhtmlContent.append("</head>\n<body>\n")
                    .append(sectionDoc.body().html())
                    .append("</body>\n</html>");

            String secaoHref = "secao_" + sectionCounter + ".xhtml";
            Resource secaoResource = new Resource(xhtmlContent.toString().getBytes(StandardCharsets.UTF_8), secaoHref);
            livro.addResource(secaoResource);
            livro.getSpine().addResource(secaoResource);
            System.out.println("Conteúdo da seção adicionado como '" + secaoHref + "'.");

            TOCReference h1Ref;
            if (capaSecaoPageResource != null) {
                h1Ref = new TOCReference(h1.text(), capaSecaoPageResource);
                System.out.println("TOC Principal: Link do H1 '" + h1.text() + "' aponta para a capa da seção.");
            } else {
                h1Ref = new TOCReference(h1.text(), secaoResource, h1.id());
                System.out.println("TOC Principal: Link do H1 '" + h1.text() + "' aponta para o conteúdo (sem capa).");
            }
            toc.addTOCReference(h1Ref);
            System.out.println("TOC Principal: Adicionado H1 -> " + h1.text());

            for (Element h2 : h2sInSection) {
                if (!h2.text().equalsIgnoreCase(h1.text())) {
                    h1Ref.getChildren().add(new TOCReference(h2.text(), secaoResource, h2.attr("id")));
                    System.out.println("TOC Principal: Adicionado H2 -> " + h2.text());
                }
            }

            sectionCounter++;
        }

        EpubWriter epubWriter = new EpubWriter();
        epubWriter.write(livro, new FileOutputStream("teste.epub"));
        System.out.println("\nEPUB gerado com sucesso: teste.epub");
    }

    private String normalizarNome(String texto) {
        return texto.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}