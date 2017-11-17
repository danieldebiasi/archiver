/*
 * Projeto Archiver
 * ST562 - Estrutura de Arquivos
 * Professor Dr. Celmar Guimarães da Silva
 */

package archiver;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Daniel Rodrigues De Biasi
 * @author Ricardo Hideki Adati Tomommitsu
 * @author Roberth Riyu Tamayose
 * @author Thiago Kaoru Alves Takehama
 */
public class Arquivo {
    
    private static final int TAM_HEADER = 110; //tamanho: 10 bytes, nome: 100 byte
    private byte[] header = new byte[TAM_HEADER];
    private byte[] conteudo;
    private File file;
    private String path;
    
    public Arquivo(){
    }
    
    public Arquivo(byte[] header, byte[] conteudo){
        this.header = header;
        this.conteudo = conteudo;
    }
    
    public Arquivo(String path){
        this.file = new File(path);
        this.path = path;
        
        String len = String.format("%010d", this.file.length());
        String name = this.file.getName();
        name = Normalizer.normalize(name, Normalizer.Form.NFD);
        name = name.replaceAll("[^\\p{ASCII}]", "");
        name = String.format("%1$-100s", name);
        String h = len.concat(name);
        try {
            this.header = h.getBytes("UTF8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Arquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Path arquivoPath = Paths.get(path);
        try {
            this.conteudo = Files.readAllBytes(arquivoPath);
        } catch (IOException ex) {
            Logger.getLogger(Arquivo.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public DefaultTableModel atualizarTabela(TableModel tableModel){
        DefaultTableModel model = new DefaultTableModel(new String[]{"Arquivo", "Tamanho (em KB)"}, 0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        
        if(tableModel.getRowCount() != 0){
            model = (DefaultTableModel)tableModel;
        }
        
        String tamanho = String.format("%.1f", (float)this.file.length()/1000);

        Object[] linha = {this.getPath(), tamanho};

        model.addRow(linha);   
        
        return model;
    }
    
    /**
     * @return the header
     */
    public byte[] getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(byte[] header) {
        this.header = header;
    }
    
    /**
     * @return the conteudo
     */
    public byte[] getConteudo() {
        return conteudo;
    }

    /**
     * @param conteudo the conteudo to set
     */
    public void setConteudo(byte[] conteudo) {
        this.conteudo = conteudo;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
}
