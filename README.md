# Gerador de EPUB a partir de HTML (Diário Oficial)

## Descrição

Este projeto é uma ferramenta em Java desenvolvida para converter um arquivo HTML específico (`diario.html`), presumidamente contendo o conteúdo do Diário Oficial de São Paulo, em um arquivo EPUB estruturado e formatado. Ele automatiza a criação de capas dinâmicas (principal e por seção), a conversão de tabelas complexas em imagens (utilizando Selenium e Firefox) para melhor compatibilidade em leitores EPUB, a adição de uma página editorial, a limpeza do HTML, a divisão em capítulos baseados nos títulos `<h1>`, a geração de sumários internos por seção (`<h2>`) e a criação do sumário principal (TOC) do EPUB.

## Status do Projeto

**Versão:** 1.0 (Funcional / Específico para o formato de entrada)

O projeto está funcional para o formato específico do arquivo `diario.html` para o qual foi projetado. Alterações na estrutura do HTML de entrada podem exigir ajustes no código. O foco principal foi a conversão e estruturação, com funcionalidades de acessibilidade no EPUB gerado sendo um resultado da estrutura correta do TOC.

## Funcionalidades

* **Conversão de Codificação:** Garante que o arquivo HTML de entrada esteja em UTF-8.
* **Geração de Capa Principal Dinâmica:** Cria uma imagem de capa PNG com data e informações da edição, utilizando um template Base64 e fontes embutidas.
* **Conversão de Tabelas para Imagens:**
    * Utiliza Selenium WebDriver com Firefox (headless) para renderizar tabelas HTML.
    * Captura screenshots de cada tabela.
    * Divide tabelas longas em múltiplas imagens (baseado em número de linhas) com cabeçalhos repetidos e títulos indicando as partes.
    * Substitui as tags `<table>` no HTML original por tags `<img>` com as imagens geradas (em Base64).
* **Processamento e Limpeza de HTML:** Utiliza Jsoup para parsear, limpar (removendo tags/atributos indesejados) e estruturar o HTML.
* **Geração de Capas de Seção:** Adiciona páginas de capa baseadas em imagens (carregadas de arquivos Base64) para seções específicas identificadas por palavras-chave nos títulos `<h1>`.
* **Adição de Página Editorial:** Inclui uma página de editorial gerada dinamicamente com informações e data atual.
* **Estruturação do EPUB:**
    * Divide o conteúdo HTML em capítulos XHTML separados, baseados nas tags `<h1>`.
    * Extrai CSS interno para um arquivo `estilos.css` separado.
    * Adiciona IDs únicos a todos os cabeçalhos (`<h1>`, `<h2>`) para navegação.
    * Gera sumários internos (mini-TOCs) com links para os `<h2>` dentro de cada capítulo `<h1>`.
* **Geração do Sumário Principal (TOC):**
    * Cria entradas no TOC (`toc.ncx` / `nav.xhtml` via epublib) para a capa principal, editorial e cada seção `<h1>`.
    * Adiciona prefixos descritivos ("Capa: ", "Conteúdo: ") às etiquetas do TOC para clareza em leitores de tela.
    * Cria entradas separadas para a capa da seção e o conteúdo da seção quando aplicável.
    * Adiciona os `<h2>` como sub-itens (filhos) da entrada de conteúdo `<h1>` correspondente no TOC.
* **Metadados do EPUB:** Adiciona informações básicas como título, autor, editora, data de publicação e idioma.
* **Empacotamento:** Gera o arquivo `diario_final.epub` final.

## Tecnologias Utilizadas

* **Linguagem:** Java (JDK 17 ou superior recomendado)
* **Bibliotecas Java:**
    * [epublib](https://github.com/psiegman/epublib): Para criação e manipulação da estrutura EPUB.
    * [Jsoup](https://jsoup.org/): Para parsing, manipulação e limpeza de HTML.
    * [Selenium WebDriver](https://www.selenium.dev/documentation/webdriver/): Para automatização do navegador (Firefox) na conversão de tabelas.
    * Java AWT (Abstract Window Toolkit): Para geração dinâmica de imagens (capa principal).
* **Dependências Externas:**
    * Navegador Mozilla Firefox instalado.
    * [GeckoDriver](https://github.com/mozilla/geckodriver/releases): O WebDriver específico para o Firefox (arquivo `geckodriver.exe` ou similar).
* **Build Tool (Provável):** Maven ou Gradle (verificar `pom.xml` ou `build.gradle` no projeto para dependências exatas).

## Como Instalar e Configurar

1.  **Pré-requisitos:**
    * Instale o JDK (Java Development Kit) versão 17 ou superior.
    * Instale o Apache Maven ou Gradle (dependendo de como o projeto gerencia dependências).
    * Instale o navegador Mozilla Firefox.
    * Baixe o executável `geckodriver` compatível com sua versão do Firefox e sistema operacional.
        * **IMPORTANTE:** Coloque o arquivo `geckodriver.exe` (ou o nome correspondente para seu SO) na pasta raiz do projeto *ou* adicione o diretório onde ele se encontra à variável de ambiente `PATH` do seu sistema.

2.  **Clone o repositório:**
    ```bash
    git clone https://github.com/Paaul0/GeradorEpubCompleto.git
    ```
3.  **Navegue até a pasta do projeto:**
    ```bash
    cd [[NOME_DA_PASTA_DO_PROJETO]] (pasta que você criou)
    ```
4.  **Coloque os Arquivos de Entrada:**
    * Coloque o arquivo HTML a ser processado com o nome `diario.html` na pasta raiz do projeto.
    * Certifique-se de que os arquivos de template Base64 (`template_base64.txt`, `OpenSans-Regular.ttf_base64.txt` e os arquivos `.txt` das capas das seções) estejam na pasta `src/main/resources` (ou onde o `CapaProvider.java` espera encontrá-los).

5.  **Compile o Projeto:**
    * Se usar Maven:
        ```bash
        mvn clean package
        ```
    * Se usar Gradle:
        ```bash
        gradlew build (ou gradlew shadowJar se configurado para criar um JAR executável)
        ```
    Isso baixará as dependências e criará um arquivo JAR (geralmente na pasta `target/` ou `build/libs/`).

## Como Usar

1.  **Certifique-se** de que todos os pré-requisitos e arquivos de entrada (`diario.html`, templates Base64, `geckodriver.exe`) estão configurados corretamente conforme a seção de instalação.
2.  **Execute o JAR compilado** a partir da linha de comando, estando na pasta raiz do projeto:
    ```bash
    java -jar target/[NOME_DO_SEU_ARQUIVO_JAR].jar
    ```
    (Ajuste o caminho `target/` e o nome do JAR conforme gerado pelo seu build tool).
3.  **Aguarde a execução.** O console mostrará o progresso das etapas: pré-processamento, geração da capa, processamento das tabelas (esta pode demorar dependendo da quantidade e tamanho), salvamento do HTML intermediário e, finalmente, a geração do EPUB.
4.  **Verifique os Arquivos de Saída:**
    * `diario_final.epub`: O arquivo EPUB gerado.
    * `diario_final.html`: O arquivo HTML intermediário com as tabelas substituídas por imagens Base64.
    * Pasta `tabelas_png_firefox/`: Contém as imagens PNG geradas a partir das tabelas (pode ser útil para depuração).

## Como Contribuir

Contribuições para melhorar a ferramenta são bem-vindas.

1.  **Faça um Fork** do repositório.
2.  **Crie uma Branch** para sua modificação (`git checkout -b feature/SuaNovaFeature`).
3.  **Faça suas alterações.** Siga as convenções de código Java e os padrões do projeto.
4.  **Faça Commit** de suas mudanças (`git commit -m 'feat(escopo): Descreve a feature'`).
5.  **Faça Push** para a sua branch (`git push origin feature/SuaNovaFeature`).
6.  **Abra um Pull Request** no repositório original.

*Considere adicionar testes unitários para novas funcionalidades ou correções.*

## Autor

**Paulo Alberto Abrahão Neto**

* **Email:** [paulo.abrahao980@gmail.com](mailto:paulo.abrahao980@gmail.com)
* **(Opcional) GitHub:** https://github.com/Paaul0
* **(Opcional) LinkedIn:** http://linkedin.com/in/paulo-neto-9b0262238
