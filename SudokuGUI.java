
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SudokuGUI implements ActionListener {

    static int[][] matrix = new int[9][9];
    SudokuSolver obj = new SudokuSolver();

    JFrame f = new JFrame();
    JButton solve = new JButton("Solve");
    JButton reset = new JButton("Reset");

    int fields_x, fields_y;
    int x = 35;
    int y = 35;
    JTextField[][] fields = new JTextField[9][9];

    {
        for (fields_y = 0; fields_y < 9; fields_y++) {
            x = 35;
            for (fields_x = 0; fields_x < 9; fields_x++) {

                fields[fields_x][fields_y] = new JTextField();
                fields[fields_x][fields_y].setBounds(x, y, 32, 32);
                f.getContentPane().add(fields[fields_x][fields_y]);
                fields[fields_x][fields_y].setHorizontalAlignment(JTextField.CENTER);
                x = x + 35;
            }
            y = y + 35;
        }
    }

    SudokuGUI() {

        solve.setBounds(110, 370, 70, 30);
        reset.setBounds(210, 370, 70, 30);
        f.add(solve);
        f.add(reset);

        solve.addActionListener(this);
        reset.addActionListener(this);
        f.setSize(400, 480);
        f.setTitle("Sudoku Solver!");
        f.setLayout(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        for (fields_y = 0; fields_y < 9; fields_y++) {
            for (fields_x = 0; fields_x < 9; fields_x++) {
                fields[fields_x][fields_y].setText("0");
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        String s;
        for (fields_y = 0; fields_y < 9; fields_y++) {
            for (fields_x = 0; fields_x < 9; fields_x++) {
                s = fields[fields_x][fields_y].getText();
                matrix[fields_x][fields_y] = Integer.parseInt(s);
            }
        }
        if (e.getSource() == solve) {
            if (checkInput(matrix)) {

                    if (obj.parseSudoku(matrix, 9)) {
                    for (fields_y = 0; fields_y < 9; fields_y++) {
                        for (fields_x = 0; fields_x < 9; fields_x++) {
                            fields[fields_x][fields_y].setText("" + matrix[fields_x][fields_y]);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(f, "The input given is not proper. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (e.getSource() == reset) {
            if (reset()) {
                for (fields_y = 0; fields_y < 9; fields_y++) {
                    for (fields_x = 0; fields_x < 9; fields_x++) {
                        fields[fields_x][fields_y].setText("0");
                    }
                }
            }
        }
    }

    public boolean reset(){
        int i,j;
        for(i=0; i<9; i++){
            for(j=0; j<9; j++){
                matrix[i][j]=0;
            }
        }
        return true;
    }

    public boolean checkInput(int[][]x) {
        int count=0;
        int i=0;
        int j=0;
        int value;
        int a;
        for (i=0;i<9;i++){
            for (j=0; j<9; j++){
                if (x[i][j] ==0){
                    count++;
                }
                if ((x[i][j] > 9)||(x[i][j]<0)){
                    return false;
                }
            }
        }
        if (count==81){
            return false;
        }
        for (i=0;i<9;i++){
            for (j=0; j<9; j++){
                if (x[i][j]!=0){
                    value = x[i][j];
                    for(a=0; a<i; a++){
                        if (x[a][j] == value){
                            return false;
                        }
                    }
                    for(a=i+1; a<9; a++){
                        if (x[a][j] == value){
                            return false;
                        }
                    }
                    for(a=0; a<j; a++){
                        if (x[i][a] == value){
                            return false;
                        }
                    }
                    for(a=j+1; a<9; a++){
                        if (x[i][a] == value){
                            return false;
                        }
                    }
                    int baseRow = i - ( i % 3 );
                    int baseCol = j - ( j % 3 );
                    for(int c=baseRow; c<baseRow+3; c++){
                        for(int b=baseCol; b<baseCol+3; b++){
                            if ((c!=i) && (b!=j)){
                                if (x[c][b]==value){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
