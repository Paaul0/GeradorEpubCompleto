package br.com.diariooficial;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GeradorImagem {
    private static final String CAMINHO_TEMPLATE = "/template.png";
    private static final String CAMINHO_FONTE = "/OpenSans-Regular.ttf";

    // Adicionado "public" para que o método seja visível para a classe Main
    public byte[] gerarImagem(DadosImagem dados) {
        try {
            // Carrega os arquivos da pasta 'resources'
            InputStream templateStream = getClass().getResourceAsStream(CAMINHO_TEMPLATE);
            InputStream fonteStream = getClass().getResourceAsStream(CAMINHO_FONTE);

            if (templateStream == null || fonteStream == null) {
                throw new IOException("Não foi possível encontrar o template ou a fonte na pasta resources.");
            }

            BufferedImage imagem = ImageIO.read(templateStream);
            Graphics2D g2d = imagem.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Carrega e define a fonte
            Font fonteBase = Font.createFont(Font.TRUETYPE_FONT, fonteStream);
            Font fonteFinal = fonteBase.deriveFont(Font.PLAIN, 108f);

            g2d.setFont(fonteFinal);
            g2d.setColor(Color.BLACK);

            // Pega o texto completo que foi passado no campo "dia" do record
            String texto = dados.dia();

            // Define a posição para desenhar o texto na imagem
            int posicaoX = 50;
            int posicaoY = 4370;

            // Desenha o texto na imagem
            g2d.drawString(texto, posicaoX, posicaoY);

            g2d.dispose();

            // Converte a imagem final para um array de bytes para ser salvo
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imagem, "png", baos);
            return baos.toByteArray();

        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Falha ao gerar a imagem.", e);
        }
    }
}