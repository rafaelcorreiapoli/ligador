package linker;

import java.io.IOException;
import java.util.StringTokenizer;

import mvn.util.LinkerSymbolTable;

/**
 * Passo 2 do ligador.<br>
 * Nesse passo é gerado o código objeto a partir da tabela
 * de símbolos obtida.
 * @author FLevy
 * @version 23.10.2006
 * Preparação do arquivo para alunos - PSMuniz 1.11.2006
 * @version 01.01.2010 : atualização da classe de acordo com a definição dos slides. (Tiago)
 */
public class Pass2 extends Pass {

    /**Gerencia o arquivo de saída*/
    private Output out;
    /**Tabela de símbolos utilizada pelo Linker*/
    private LinkerSymbolTable symbolTable;
    /**Indica o endereçamento corrente da parte relocável do código. */
    private int relativeLocationCouter;
    /**A base de relocação a ser considerada no código. */
    private int base;

    /**Contador de variáveis externas que não foram resolvidas*/
    private int externalCounter = 0;


    public Pass2(LinkerSymbolTable symbolTable, String objFile) throws IOException {
        out = new Output(objFile);
        this.symbolTable = symbolTable;
        relativeLocationCouter = 0;
        base = 0;
    }

    /**  NADDR CODE
     * Processa uma linha de código.
     *
     * @param nibble O nibble do endereço da linha.
     * @param address O endereço da linha (sem o nibble).
     * @param code O código da linha.
     * @param currentFile O arquivo atual que está sendo processado.
     * @return Verdadeiro caso a análise teve sucesso, falso caso contrario.
     * @exception Caso tenha ocorrido algum problema de IO.
     */
    protected boolean processCode(int nibble, String address, String code, String currentFile)
            throws IOException {

        int inteiro1 = Integer.parseInt(address, 16);
        String string1 = code;
        boolean bool1 = false;
        if(isArgumentRelocable(nibble)) {
            bool1 = true;
        }

        boolean bool2 = false;
        if(isRelocable(nibble)) {
            inteiro1 += this.base;
            bool2 = true;
            this.relativeLocationCouter += 2;
        }

        int inteiro2 = Integer.parseInt(code.substring(1), 16);
        String string2 = this.symbolTable.getSymbol(currentFile, inteiro2);
        boolean bool3 = true;
        if(string2 != null && string2.startsWith("5") && (nibble == 5 || nibble == 13)) {
            bool3 = false;
        }

        if((string2 = (string2 = "0000" + string2).substring(string2.length() - 3, string2.length())) != null) {
            if(nibble % 2 == 1) {
                LinkerSymbolTable tabela;
                bool1 = (tabela = this.symbolTable).isRelocable(tabela.getAddressByCode(currentFile, inteiro2));
                string1 = code.substring(0, 1) + string2;
            }

            if((nibble >> 2) % 2 != 0) {
                string1 = code.substring(0, 1) + string2;
            }
        } else if(nibble % 2 == 1) {
            string1 = code;
        }

        if(isArgumentRelocable(nibble)) {
            nibble = Integer.parseInt(string1, 16) + this.d;
            string1 = (string1 = "0000" + Integer.toHexString(nibble)).substring(string1.length() - 4, string1.length());
        }

        this.out.write(inteiro1, string1, bool2, bool1, bool3);
        return true;
       /*
         * TODO: processCode
         *
         * Aqui, deve-se gerar o código objeto a partir da tabela de simbolos (ST).
         * Deve-se avaliar as combinações apropriadas do nibble. Se houver pendência (relocável
         * ou absoluta), ela deve ter ser resolvida e seu valor inserido na ST.
         * O código resolvido deve ser enviado para a saída.
         *
         */
    }

    protected boolean processSymbolicalAddress(int nibble, String address, String symbol, String currentFile, String originalLine)
            throws IOException {

        /**
         * TODO: processSymbolicalAddress
         * Tratamento do Endereçamento simbólico.
         * Caso EntryPoint: escreve no arquivo de saída
         * Caso External: se resolvido ignora, caso contrário insere na tabela o arquivo com um novo endereçamento "virtual"
         *                  e escreve o external no arquivo de saída.
         *
         *
         */

        //Se for símbolo exportável: gero no arquivo de saída as informações a respeito dele
        //...
        //...

        if(isEntryPoint(nibble)){
            this.out.writeExternal(Integer.toHexString(nibble), Integer.parseInt(this.symbolTable.getSymbolValue(symbol), 16), originalLine);
        } else {
            if(!this.symbolTable.symbolInTable(symbol)) {
                this.symbolTable.definedSymbol(symbol);
                String local = "0000" + Integer.toHexString(this.base);
                local = "5" + local.substring(local.length() - 3, local.length());
                this.symbolTable.setSymbolValue(symbol,local);
                this.out.writeExternal("4", Integer.parseInt(this.symbolTable.getSymbolValue(symbol), 16), originalLine);
                ++this.externalCounter;
            }

            StringTokenizer palavra = new StringTokenizer(originalLine);
            this.symbolTable.setCodeForSymbol(symbol, currentFile, Integer.parseInt(palavra.nextToken().substring(1, 4), 16));
        }
        return true;
    }

    /**
     * Finaliza o arquivo lido (pode haver um próximo arquivo).
     */
    protected void fileEnd() {
        /*
         * TODO: fileEnd()
         * Quando há mudança de arquivo, deve-se atualizar a base e o relativeLocationCounter!
         * */
         this.base += relativeLocationCouter;
         this.relativeLocationCouter = 0;
    }

    public void closeOutput() throws IOException {
        out.close();
    }
}
