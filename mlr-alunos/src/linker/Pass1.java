package linker;

import java.io.IOException;

import mvn.util.LinkerSymbolTable;

/**
 * Passo 1 do ligador.<br>
 * Nesse passo é criada a tabela de símbolos do linker, passando por todos os
 * arquivos.<br>
 * Por questões de simplicidade, existem algumas simplificações no linker:
 * - cada arquivo apresenta uma e apenas uma origem relocável;
 * - os conflitos de código absoluto não são verificados;
 * @author FLevy
 * @version 23.10.2006
 * Preparação do arquivo para alunos - PSMuniz 1.11.2006
 * @version 01.01.2010 : atualização da classe de acordo com a definição dos slides (Tiago)
 */
public class Pass1 extends Pass {

    /**A tabela de símbolos para o linker*/
    private LinkerSymbolTable symbolTable;
    /**A posição relocável anterior*/
    private int relativeLocationCouter;
    /**A base de relocação a ser considerada no código*/
    private int base;
    /**Numera as diferentes variáveis externas existentes num arquivo*/
    private int externalCounter = 0;

    public Pass1() {
        symbolTable = new LinkerSymbolTable();
        relativeLocationCouter = 0;
        base = 0;
    }

    /**
     * Processa uma linha de código.
     *
     * @param nibble O nibble com as informações do endereço da linha.
     * @param address O endereço da linha (sem o nibble).
     * @param code O código da linha.
     * @param currentFile O arquivo atual que está sendo processado.
     * @return Verdadeiro caso a análise teve sucesso, falso caso contrário.
     * @exception Caso tenha ocorrido algum problema de IO.
     */
    protected boolean processCode(int nibble, String address, String code, String currentFile){
        /**
         * TODO: processCode
         *
         * Analisar o nibble e incrementar o endereçamento relativo (relativeLocationCounter) de acordo.
         *
         */
        try {
             if (isRelocableEntryPoint(nibble) == true) {
               this.relativeLocationCounter += 2;
               return true;
             }
            catch (IOException e) {
            }
            return false;
    }//

    /**
     * Processa uma linha de endereço simbólico.
     *
     * @param nibble O nibble com as informações do endereço da linha.
     * @param address O endereço referente ao símbolo (sem nibble).
     * @param symbol O símbolo em si.
     * @param currentFile O arquivo atual que esta sendo processado.
     * @return Verdadeiro caso a análise teve sucesso, falso caso contrário.
     * @throws IOException
     *             Caso tenha ocorrido algum problema de IO.
     */
    protected boolean processSymbolicalAddress(int nibble, String address, String symbol, String currentFile, String originalLine)
            throws IOException {
        /*

         * TODO: processSymbolicalAddress
         * Situações típicas: entry point relocável, entry point absoluto e external.
         * O objetivo é resolver os entry points e externals relativos.
         * Devem ser avaliadas as combinações apropriadas do nibble.
         * Deve-se calcular o endereço do codigo e atualizar apropriadamente a tabela de simbolos.
         */
        if(isRelocableEntryPoint(nibble) == true) {
            this.symbolTable.getSymbolValue(symbol);
            String new_address = Integer.toHexString(Integer.parseInt(address,16) + this.base);
            this.symbolTable.setSymbolValue(symbol,new_address,true);
        }
        else if(isEntryPoint(nibble) == true) {
            this.symbolTable.getSymbolValue(symbol);
            this.symbolTable.setSymbolValue(symbol,address);
        }
        else {
            if(isExternalPseudoInstruction(nibble) == false) {
                return false;
            }

            this.symbolTable.setCodeForSymbol(symbol,address,this.base);
            ++this.base;
        }
        return true;

    }//

    /**
     * Finaliza o arquivo lido (pode haver um próximo arquivo).
     */
    protected void fileEnd() {

        /*
         * TODO: fileEnd()
         * Quando há mudança de arquivo, deve-se mudar a base!
         * Atualizar as variáveis existentes de acordo.
         *
         * */

         this.base += relativeLocationCouter;
         this.relativeLocationCouter = 0;
         this.externalCounter = 0;
    }//

    public LinkerSymbolTable getSymbolTable() {
        return symbolTable;
    }
}
