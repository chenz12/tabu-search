package Tabu;

/**
 * Created by panzer on 2016/3/2.
 */
public class main {
    public static void main(String[] args) throws Exception{
        tabu t = new tabu("pr01",400,1,20);
        t.solve();
        for (int i: t.number
                ) {
            System.out.println(i);
        }
        System.out.println(t.duration(t.best));
        System.out.println(t.all_dis(t.best));
        System.out.println();
        for(int i=0;i<t.best.rout.size();i++){
            for (int j=0;j<t.best.rout.get(i).size();j++){
                System.out.print(t.best.rout.get(i).get(j).B+"     ");
                System.out.println(t.best.rout.get(i).get(j).index);

            }
        }
        System.out.println();
    }
}
