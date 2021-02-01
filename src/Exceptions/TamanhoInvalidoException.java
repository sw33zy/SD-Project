package Exceptions;

public class TamanhoInvalidoException extends Exception{
    private int tamanho;

    public TamanhoInvalidoException(int tamanho){
        this.tamanho=tamanho;
    }

    public int getTamanho(){
        return this.tamanho;
    }
}
