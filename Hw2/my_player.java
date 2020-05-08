import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class my_player {
    private static final int BLACKMOVE = 1;
    private static final int WHITEMOVE = 2;

    private static final String TYPEMOVE = "MOVE";
    private static final String TYPEPASS = "PASS";
    private static final int BOARDSIZE = 5;

    private static final int [][] direction = {
            {1,0}, {0,1}, {-1,0}, {0,-1}
    };

    public static class Point {
        private int X;
        private int Y;
        private int type;
        private int [][] next_board;
        private int score;
        private boolean moveOrPass;
        private int sort_rank;
        private int score_die;
        private int free_places;


        Point(int x, int y){
            this.X = x;
            this.Y = y;
        }

        Point(int x, int y, int sort_rank){
            this.X = x;
            this.Y = y;
            this.sort_rank = sort_rank;
        }

        Point(int x, int y, int type, int[][] next, boolean move){
            this.X = x;
            this.Y = y;
            this.type = type;
            this.next_board = next;
            this.moveOrPass = move;
        }

        public void setSort_rank(int sort_rank) {
            this.sort_rank = sort_rank;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public void setScore_die(int score_die) {
            this.score_die = score_die;
        }

        public void setFree_places(int free_places) {
            this.free_places = free_places;
        }

        public int getX() {
            return X;
        }

        public int getY() {
            return Y;
        }

        public int getScore() {
            return score;
        }

        public int getScore_die() {
            return score_die;
        }

        public int[][] getNext_board() {
            return next_board;
        }

        public int getType() {
            return type;
        }

        public int getFree_places() {
            return free_places;
        }

        public int getSort_rank() {
            return sort_rank;
        }

        public boolean isMoveOrPass() {
            return moveOrPass;
        }

        public void print(){
            System.out.println(X + " " + Y + " Score:" + score);
            for(int i = 0; i< 5; i ++){
                for (int j = 0; j< 5 ; j++){
                    System.out.print(next_board[i][j]);
                }
                System.out.println();
            }

        }

        public void printRank(){
            System.out.println("Score:" + score);
            System.out.println("Rank:" + sort_rank);
            System.out.println("die:" + score_die);
        }

    }

    public static class Move {
        private int MoveType;
        private int X;
        private int Y;

        Move(){

        }

        void setMove(int type, int x, int y){
            this.MoveType = type;
            this.X = x;
            this.Y = y;
        }

        /**
         * @param type 1 (MOVE) 2(PASS)
         * @param x move to x
         * @param y move to y
         */
        Move(int type, int x, int y){
            this.MoveType = type;
            this.X = x;
            this.Y = y;
        }

        boolean isPass(){
            return MoveType == 2;
        }

        String getMoveType() {
            if (isPass()) return TYPEPASS;
            return TYPEMOVE;
        }

        int getX() {
            return X;
        }

        int getY() {
            return Y;
        }

        public String toString(){
            return X + "," + Y;
        }
    }

    public static class Board {


        private int size;
        private int piece_type;
        private int max_move;
        private double komi;
        private int [][] pre_board;
        private int [][] cur_board;
        private ArrayList<Point> next_step;
        private int [][] test_board;


        public Board(int n){
            this.size = n;
            this.max_move = n * n -1;
            this.komi = size/2;
        }

        public void set_board(int piece_type, int[][] pre_board, int[][] cur_board) {
            this.piece_type = piece_type;
            this.pre_board = pre_board;
            this.cur_board = cur_board;
            this.next_step = new ArrayList<>();
        }

        public ArrayList<Point> get_available_board(){
            for(int i = 0; i< size; i ++){
                for (int j = 0; j< size ; j++){
                    if ( is_valid_place(i, j, piece_type)){
                        next_step.add(new Point(i, j, piece_type, test_board, true));
                    }
                }
            }

            if (!compare_board(pre_board, cur_board)){
                next_step.add(new Point(-1,-1, piece_type, cur_board, false));
            }
            return next_step;
        }

        public boolean is_valid_place(int i, int j, int piece_type){
            //Check if the palce is in the board
            if (i < 0 || i >= size)
                return false;
            if (j < 0 || j >= size)
                return false;

            //Check if the place already has a place
            if (cur_board[i][j] != 0)
                return false;

            //Check if the place has liberty
            test_board = copy_board(cur_board);
            test_board[i][j] = piece_type;
            if (find_liberty(test_board, i, j)){
                remove_died_pieces(3- piece_type);
                return true;
            }


            boolean died = remove_died_pieces(3- piece_type);
            // Check if the place is sucide
            if (!find_liberty(test_board, i, j))
                return false;
            // Check if the obey KO rule
            if (died && compare_board(test_board, pre_board))
                return false;
            return true;
        }


        int [][] copy_board(int [][] board){
            int [][] copy_one = new int [size][size];
            for(int i = 0; i< size; i ++){
                for (int j = 0; j< size ; j++){
                    copy_one[i][j] = board[i][j];
                }
            }
            return  copy_one;
        }

        boolean find_liberty(int[][] b, int i, int j){
            int type = b[i][j];
            Queue<Point> q = new LinkedList<Point>();
            q.add(new Point(i,j));
            boolean [][] visit = new boolean[size][size];
            while(!q.isEmpty()){
                Point p = q.poll();
                if (p.X < 0 || p.X >= size || p.Y < 0 || p.Y >= size)
                    continue;
                if (visit[p.X][p.Y])
                    continue;
                if (b[p.X][p.Y] == 0)
                    return true;
                if (b[p.X][p.Y] == type){
                    visit[p.X][p.Y] = true;
                    for (int n =0; n < 4; n++){
                        q.add(new Point(p.X + direction[n][0], p.Y + direction[n][1]));
                    }
                }
            }
            return false;
        }

        boolean remove_died_pieces(int type){
            for (int i = 0; i < size; i++){
                for (int j =0; j< size; j++){
                    if(test_board[i][j] == type){
                        if (!find_liberty(test_board, i , j)){
                            piece_dfs(i, j, type);
                        }
                    }
                }
            }
            boolean remove = false;
            for (int i = 0; i < size; i++){
                for (int j =0; j< size; j++){
                    if (test_board[i][j] < 0){
                        test_board[i][j] = 0;
                        remove = true;
                    }
                }
            }
            return remove;
        }

        void piece_dfs( int i, int j, int type){
            if (i < 0 || i >= size || j < 0 || j >= size)
                return;
            if (test_board[i][j] != type)
                return;
            if (test_board[i][j] == type) test_board[i][j] = -1;
            for (int n = 0; n <4 ; n++){
                piece_dfs( i + direction[n][0], j+direction[n][1], type);
            }
        }

        /**
         * compare two boards
         * check if PASS happened
         * check if match KO rule
         * */
        boolean compare_board(int[][] board1, int[][] board2){
            for(int i = 0; i< size; i ++){
                for (int j = 0; j< size ; j++){
                    if (board1[i][j] != board2[i][j]) return false;
                }
            }
            return true;
        }

        public void printBoard(){
            for(int i = 0; i< 5; i ++){
                for (int j = 0; j< 5 ; j++){
                    System.out.print(cur_board[i][j]);
                }
                System.out.println();
            }
        }

        public ArrayList<Point> getNext_step() {
            return next_step;
        }

        int[][] getPre_board() {
            return pre_board;
        }

        int[][] getCur_board() {
            return cur_board;
        }
    }

    static int [][] score_table = {{1,2,2,2,1}, {2,4,3,4,2}, {2,3,5,3,2}, {2,4,3,4,2}, {1,2,2,2,1}};
    static int [][] score_table_forWhite = {{1,2,2,2,1}, {2,3,4,3,2}, {2,4,5,4,2}, {2,3,4,3,2}, {1,2,2,2,1}};



    public static void main(String[] args) throws IOException {
        int [][]input = readInput(BOARDSIZE);
        int [][]board = new int [BOARDSIZE][BOARDSIZE];
        int [][]pre_board = new int [BOARDSIZE][BOARDSIZE];
        int type = input[2*BOARDSIZE][0];
        for(int n = 0; n < 2*BOARDSIZE; n++){
            if (n<BOARDSIZE){
                pre_board[n] = input[n];
            }else{
                board[n-BOARDSIZE] = input[n];
            }
        }

        Board go = new Board(BOARDSIZE);

        go.set_board(type, pre_board, board);

        ArrayList<Point> next = go.getNext_step();

        getMaxValue(type, go, Integer.MIN_VALUE, Integer.MAX_VALUE ,4);


        PriorityQueue<Point> pq = new PriorityQueue<>(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                if (o1.getScore() == o2.getScore()){
                    if (o1.getSort_rank() == o2.getSort_rank()){
                        return o2.getScore_die() - o1.getScore_die();
                    }else {
                        return o2.getSort_rank() - o1.getSort_rank();
                    }
                }else{
                    return o2.getScore() - o1.getScore();
                }
            }
        });

        for (Point p : next){
            rank(p);
            pq.add(p);
        }

        Move out = new Move();

        if (pq.peek().isMoveOrPass()){
            out.setMove(1, pq.peek().getX(), pq.peek().getY());
        }else {
            out.setMove(2, pq.peek().getX(), pq.peek().getY());
        }

        writeOutput(out);
    }

    public static void rank(Point point){
        int myScore_forBlack = 0;
        int myScore_forWhite = 0;
        int Score = 0;
        int type = point.getType();
        int [][] board = point.getNext_board();

        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board.length; j++){
                if (board[i][j] == type){
                    myScore_forBlack += score_table[i][j];
                    myScore_forWhite += score_table_forWhite[i][j];
                    Score ++;
                }else {
                    myScore_forBlack -= score_table[i][j];
                    myScore_forWhite -= score_table_forWhite[i][j];
                    Score --;
                }
            }
        }


        if (point.getType() == BLACKMOVE){
            point.setSort_rank(Score + myScore_forBlack);
            point.setScore_die(myScore_forBlack);
        }else {
            point.setSort_rank(Score + myScore_forWhite);
            point.setScore_die(myScore_forWhite);
        }

    }

    public static int getMinValue(int type, Board go, int min, int max, int deepth){
        if (deepth == 0){
            return evaluate_score(type, go.getCur_board());
        }

        int value = Integer.MAX_VALUE;
        ArrayList<Point> next = go.get_available_board();
        for (Point p : next){
            go.set_board(3-type, go.getCur_board(), p.next_board);
            int num = getMaxValue(3 - type, go, min, max, deepth -1);
            value = Math.min(value, num);
            p.setScore(num);
            //go.set_board(type, go.getPre_board(), go.getCur_board());
            if (min >= value){
                break;
            }
//            max = Math.min(max, value);
        }
        return value;
    }

    public static int getMaxValue(int type, Board go, int min, int max, int deepth){
        if (deepth == 0){
            return evaluate_score(type, go.getCur_board());
        }

        ArrayList<Point> next = go.get_available_board();
        int value = Integer.MIN_VALUE;
        for (Point p : next){
            go.set_board(3-type, go.getCur_board(), p.next_board);
            int num = getMinValue( 3- type, go, min, max,deepth-1);
            value = Math.max(value, num);
            p.setScore(num);
//            go.set_board(type, go.getPre_board(), go.getCur_board());
            if (max <= value){
                break;
            }
//            min = Math.max(min, value);
        }
        return value;
    }

    public static int evaluate_score(int type, int[][] board){
        int myScore = 0;
        int taScore = 0;

        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board.length; j++){
                if (board[i][j] == type)
                    myScore++;
                else if (board[i][j] == 3 -type)
                    taScore++;
            }
        }
        return myScore - taScore;
    }

    private static int[][] readInput(int n)  throws IOException{
        File file = new File("input.txt");
        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8), 1024 * 1024);
        String line = reader.readLine();
        String piece_type = line.trim();

        int [][] board = new int [2*n+1][n];
        for(int i = 0 ; i < 2*n; i ++){
            String[] row = reader.readLine().trim().split("");
            for (int j = 0; j < n; j++) {
                board[i][j] = Integer.parseInt(row[j]);
            }
        }
        board[2*n][0] = Integer.parseInt(piece_type);

        fis.close();
        reader.close();
        return board;
    }

    public static void writeOutput(Move move) throws IOException{

        FileWriter file = new FileWriter( new File("output.txt"));
        if (move.isPass()){
            file.write(move.getMoveType());
        }else{
            file.write(move.toString());
        }
        file.close();
    }
}
