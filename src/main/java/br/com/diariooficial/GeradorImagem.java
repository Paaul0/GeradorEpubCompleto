package br.com.diariooficial;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class GeradorImagem {

    public byte[] gerarImagem(String textoParaCapa) {
        try {
            // Decodifica o template e a fonte de Base64 para arrays de bytes.
            byte[] templateBytes = Base64.getDecoder().decode(CapaProvider.getCapaPrincipalTemplateBase64());
            byte[] fonteBytes = Base64.getDecoder().decode(CapaProvider.getFonteBase64());

            // Cria InputStreams a partir dos arrays de bytes em memória.
            InputStream templateStream = new ByteArrayInputStream(templateBytes);
            InputStream fonteStream = new ByteArrayInputStream(fonteBytes);

            BufferedImage imagem = ImageIO.read(templateStream);
            Graphics2D g2d = imagem.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Carrega a fonte a partir do stream.
            Font fonteBase = Font.createFont(Font.TRUETYPE_FONT, fonteStream);
            Font fonteFinal = fonteBase.deriveFont(Font.PLAIN, 108f);

            g2d.setFont(fonteFinal);
            g2d.setColor(Color.BLACK);

            // Define a posição para desenhar o texto na imagem.
            int posicaoX = 50;
            int posicaoY = 4370;

            // Desenha o texto recebido como argumento.
            g2d.drawString(textoParaCapa, posicaoX, posicaoY);
            g2d.dispose();

            // Converte a imagem final (com o texto) para um array de bytes.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imagem, "png", baos);
            return baos.toByteArray();

        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Falha ao gerar a imagem a partir do template Base64.", e);
        }
    }
}