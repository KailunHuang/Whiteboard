package CreateWhiteBoard;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class TEXT {
    public static void main(String[] args) {
        int j = 0;
        int lose = 0;
        int sum = 0;
        while (j <1) {
            j++;
            int i = 0;
            int x = 1000;
            int money = x;
            int tmp = 100;
            while (i < 50 && money > 0) {
                i++;
                money -= tmp;
                if (money <= 0) {
                    lose++;
                } else {
                    Random rand = new Random();
                    if ((rand.nextInt(11) + 1) % 2 == 1) { //成功
                        // System.out.println("win " + tmp);
                        money += tmp * 2;
                        tmp = 100;
                    } else {
                        // System.out.println("lose " + tmp);
                        tmp = tmp * 2;
                    }
                }
            }

            if(money > 0 && money < x){
                lose++;
            }
            sum += money-x;


        }
        System.out.println("lose "+ lose + ", sum "+sum);
    }
}
