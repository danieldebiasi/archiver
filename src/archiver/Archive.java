/*
 * Projeto Archiver
 * ST562 - Estrutura de Arquivos
 * Professor Dr. Celmar Guimarães da Silva
 */

package archiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Daniel Rodrigues De Biasi
 * @author Ricardo Hideki Adati Tomommitsu
 * @author Roberth Riyu Tamayose
 * @author Thiago Kaoru Alves Takehama
 */
public class Archive {

    private static final int TAM_HEADER = 8; //Quantidade de arquivos no archive
    private List<Arquivo> arquivos;
    private String path;
    
    public Archive(){
    }
    
    public Archive(String path){
        this.path = path;
        this.arquivos = obterTodos();
    }
    
    public DefaultTableModel atualizarTabela(){
        DefaultTableModel model = new DefaultTableModel(new String[]{"Nome", "Tamanho (em KB)"}, 0){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        
        for (Arquivo arquivo : this.arquivos) {
            byte[] header = arquivo.getHeader();            
            Arrays.copyOfRange(header, 0, 10);
            float tam = Float.parseFloat(new String(Arrays.copyOfRange(header, 0, 10)))/1000;
            String tamanho = String.format("%.1f", tam);
            String nome = new String((byte[])Arrays.copyOfRange(header, 10, 110)).replaceAll("\\s+$", "");
            
            Object[] linha = {nome, tamanho};
            
            model.addRow(linha);
        }       
        
        return model;
    }
    
    public boolean criar(String path, String name){
        String filePath = path+File.separator+name+".sky";
        File file = new File(filePath);
        if(file.exists()){
            return false;
        }else{
            try {
                file.getParentFile().mkdir();
                file.createNewFile();
                
                RandomAccessFile r = new RandomAccessFile(file, "rw");
                r.seek(0);
                r.write(String.format("%08d", 0).getBytes());
                r.close();            
                
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
    }
    
    public boolean inserir(String path, String pathArquivo){
        Arquivo arquivo = new Arquivo(pathArquivo);
        File archive = new File(path);
        
        if(new File(pathArquivo).length() <= 1000000000){
            try {
                RandomAccessFile r = new RandomAccessFile(archive, "rw");            
                byte[] header = new byte[TAM_HEADER];

                //Atualiza header do archive
                r.read(header);
                r.seek(0);
                r.writeBytes(String.format("%08d", Integer.parseInt(new String(header))+1));

                //Junta header e conteúdo para inserção no archive
                byte[] arq = new byte[arquivo.getHeader().length + arquivo.getConteudo().length];
                System.arraycopy(arquivo.getHeader(), 0, arq, 0, arquivo.getHeader().length);
                System.arraycopy(arquivo.getConteudo(), 0, arq, arquivo.getHeader().length, arquivo.getConteudo().length);

                r.seek(archive.length());
                r.write(arq, 0, arq.length);
                r.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return true;
        }else{
            return false;
        }
    }
    
    public boolean extrair(String nome, String diretorio){
        boolean ret = false;
       
        if(!arquivos.isEmpty()){            
            for(Arquivo arquivo : this.arquivos){
                byte[] header = arquivo.getHeader();
                if(nome.equals(new String(Arrays.copyOfRange(header, 10, 110)).replaceAll("\\s+$", ""))){
                    try {
                        FileOutputStream output = new FileOutputStream(diretorio+"\\"+nome, true);
                        output.write(arquivo.getConteudo());
                        output.close();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    ret = true;
                    break;
                }
            }
        }
        
        return ret;
    }
    
    public boolean extrairTudo(String diretorio){
        boolean ret = false;
       
        if(!arquivos.isEmpty()){            
            for(Arquivo arquivo : this.arquivos){
                byte[] header = arquivo.getHeader();                
                try {
                    String nome = new String(Arrays.copyOfRange(header, 10, 110)).replaceAll("\\s+$", "");
                    FileOutputStream output = new FileOutputStream(diretorio+File.separator+nome, true);
                    output.write(arquivo.getConteudo());
                    output.close();
                    ret = true;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
                    ret = false;
                } catch (IOException ex) {
                    Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
                    ret = false;
                }              
            }
        }
        
        return ret;
    }
    
    public boolean remover(String nome){
        boolean ret = false;
        File archive = new File(this.path);
        
        try {
            RandomAccessFile r = new RandomAccessFile(archive, "rw");            
            byte[] header = new byte[TAM_HEADER];
            
            //Atualiza header do archive
            r.read(header);
            r.close();
            if(archive.delete());
            archive.getParentFile().mkdir();
            archive.createNewFile();
            r = new RandomAccessFile(archive, "rw");
            r.seek(0);
            r.writeBytes(String.format("%08d", Integer.parseInt(new String(header))-1));
            
            for(Arquivo arquivo : this.arquivos){
                header = arquivo.getHeader();
                if(nome.equals(new String(Arrays.copyOfRange(header, 10, 110)).replaceAll("\\s+$", ""))){
                }else{
                    byte[] arq = new byte[arquivo.getHeader().length + arquivo.getConteudo().length];
                    System.arraycopy(arquivo.getHeader(), 0, arq, 0, arquivo.getHeader().length);
                    System.arraycopy(arquivo.getConteudo(), 0, arq, arquivo.getHeader().length, arquivo.getConteudo().length);
                    r.write(arq);
                }                
            }
            ret = true;
            r.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } catch (IOException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        
        return ret;
    }
    
    public boolean removerTudo(){
        boolean ret = false;
        File archive = new File(this.path);
        
        try {
            RandomAccessFile r = new RandomAccessFile(archive, "rw");            
            byte[] header = new byte[TAM_HEADER];
            
            //Atualiza header do archive
            r.read(header);
            r.close();
            if(archive.delete());
            archive.getParentFile().mkdir();
            archive.createNewFile();
            r = new RandomAccessFile(archive, "rw");
            r.seek(0);
            r.writeBytes(String.format("%08d", 0));
            
            ret = true;
            r.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        } catch (IOException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        
        return ret;
    }
    
    public List<Arquivo> obterTodos(){
        List<Arquivo> arquivos = new ArrayList<>();
        
        try {            
            RandomAccessFile r = new RandomAccessFile(this.path, "r");
            byte[] b = new byte[TAM_HEADER];            
            r.read(b);
            
            int total = Integer.parseInt(new String(b));
            
            for(int i = 0; i < total; i++){
                byte[] header = new byte[110];
                r.read(header);
                
                int tamanho = Integer.parseInt(new String(Arrays.copyOfRange(header, 0, 10)));
                
                byte[] conteudo = new byte[tamanho];
                r.read(conteudo);

                arquivos.add(new Arquivo(header, conteudo));
            }
            
            r.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Archive.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return arquivos;
    }
    
}
