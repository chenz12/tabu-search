package Tabu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by panzer on 2016/2/27.
 */
public class tabu {
    demand[] demands;
    int max_gen;
    int max_once;
    int car_num;
    int demand_num;
    int[] max_time;      //线路最长运营时间
    int[] capacity;
    int max_travel_time; //旅客最长旅行时间
    double[][] distance;
    attr[] jinji;

    solution best;
    double best_cost;
    solution local_best;
    double local_best_cost;

    ArrayList<solution> neighbor;
    ArrayList<Integer> number = new ArrayList<Integer>();

    int num_a;
    int num_b;

    static double c1 = 100;
    static double c2 = 3;
    static double c3 = 4;
    static double c4 = 100;
    static double c5 = 2;

    tabu(String s, int i,int j, int k) throws Exception{
        init(s);
        distmatri();
        this.max_gen = i;
        this.max_once =j;
        jinji = new attr[k];
        best = new solution();
    }
    double duration(solution s){
        double res=0;
        for(int i=0;i<s.rout.size();i++){
            res += s.rout.get(i).get(s.rout.get(i).size()-1).A-s.rout.get(i).get(0).D;
        }
        return res;
    }

    double all_dis(solution s){
        return s.dis();
    }

    double cost(solution so) {
        //double c_cost = 0; //运营成本
       // double q_cost = 0; //载客量超出成本
       // double d_cost = 0; //服务时间缺少成本
       // double w_cost = 0; //时间窗超出成本
       // double t_cost=0; //旅行时间超出成本

        return all_dis(so)*c1+so.load()*c2+so.duration()*c3+so.window()*c4+so.ride()*c5;
/*
        int m = so.s.size();
        for (int i = 0; i < m; i++) {
            for (pair a : so.s.get(i)
                    ) {
                c_cost += distance[a.pre][a.suc];
                q_cost += over(a.num - capacity[i]);
                d_cost += over(a.lack_servece);
                w_cost += over(a.time_window);
            }
        }
        return c_cost + q_cost + d_cost + w_cost + so.sum(so.travel);
*/
    }


    void distmatri() {
        int num = demands.length;
        distance = new double[num][num];
        for (int i = 0; i < num; i++) {
            for (int j = 0; j < num; j++) {
                distance[i][j] = dist(demands[i], demands[j]);
            }
        }
    }

    double dist(demand a, demand b) {
        return Math.pow(Math.pow(a.lat - b.lat, 2) + Math.pow(a.lon - b.lon, 2), 0.5);
    }
    /*
    int over(int a) {
        if (a > 0) {
            return a;
        } else return 0;
    }

    double over(double a) {
        if (a > 0) {
            return a;
        } else return 0;
    }*/
/*
    attr miaoshi(){
        for (attr a : jinji){
            for (int i=0;i<car_num;i++){
                if (i!=a.route_num){

                }
            }
        }
        return null;
    }
*/
    void add_jinji(attr a){
        for (int i=1;i<jinji.length;i++){
            jinji[i-1] = jinji[i];
        }
        jinji[jinji.length-1]=a;
    }

    boolean contain_jinji(attr a){
        for (int i=0;i<jinji.length;i++){
            if (a.equals(jinji[i])){
                return true;
            }
        }
        return false;
    }




    void solve() {
        best = new solution();
        best_cost = cost(best);
        best.fresh_attr();
        local_best = (solution)best.clone();
        local_best_cost = best_cost;
        solution temp;
        temp = (solution)best.clone();
        int n=0;
        Random r1 = new Random();
        Random r2 = new Random();
        while(n<max_gen){
            attr b = null;
            attr local = null;
            int nn=0;
            while(nn<max_once) {

                attr a = temp.att.get(r2.nextInt(temp.att.size()));
                while (!(!contain_jinji(a)&&!a.equals(b)&&a.index<=demand_num/2)) {
                    a = temp.att.get(r2.nextInt(temp.att.size()));
                }
                neighbor = temp.neighbor(a);
                int ten = r1.nextInt(car_num-1);
                temp = neighbor.get(ten);
                temp.fresh_attr();
                if(0==a.route_num){
                    num_b = ten+1;
                }
                else if(1==a.route_num){
                    if(ten == 0){num_b=0;}
                    else{num_b=2;}
                }
                else{num_b=ten;}
                temp.sort(num_a,num_b);
                double test = cost(temp);
                if (cost(temp) < local_best_cost) {
                    local_best = (solution)temp.clone();
                    local_best_cost = cost(temp);

                }
                local = (attr)a.clone();
                b = a;
                add_jinji(local);
                nn++;
            }

            if (local_best_cost<best_cost){
                number.add(n);
                best = (solution)local_best.clone();
                best_cost = local_best_cost;
            }
            if (n%10==0){
                best = best.intra_route();
                best_cost = cost(best);
                temp = (solution)best.clone();
            }

            n++;
            /*
            if(local!=null) {
                add_jinji(local);
            }*/
        }


    }

    void init(String s) throws Exception{
        File f = new File(s);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String[] n1 = br.readLine().split("\\s+");
        car_num = Integer.valueOf(n1[0].trim());
        demand_num = Integer.valueOf(n1[1].trim());
        max_time = new int[car_num];
        capacity = new int[car_num];
        for (int i=0;i<car_num;i++){
            max_time[i] = Integer.valueOf(n1[2].trim());
            capacity[i] = Integer.valueOf(n1[3].trim());
        }
        max_travel_time = Integer.valueOf(n1[4].trim());
        demands = new demand[demand_num+1];
        for (int i=0;i<=demand_num;i++){
            String tem = br.readLine();
            //System.out.println(tem);
            demands[i] = new demand(tem,i);
        }
    }


    public class solution implements Cloneable{
        ArrayList<ArrayList<vertex>> rout;
        ArrayList<attr> att;

        solution() {
            rout = new ArrayList<ArrayList<vertex>>(car_num);
            for(int i=0;i<car_num;i++){
                rout.add(new ArrayList<vertex>());
            }
            Random r = new Random();
            for (demand d : demands) {
                if (0<d.index&&d.index<=demand_num/2){
                    int te = r.nextInt(car_num);
                    //System.out.println(te);
                    rout.get(te).add(new vertex(d.index));
                    rout.get(te).add(new vertex(24+d.index));
                }
            }
            for(int i=0;i<rout.size();i++){
                rout.get(i).add(0,new vertex(0));
                rout.get(i).add(new vertex(0));
            }
            att = new ArrayList<attr>(demand_num);
            init_attr();
            sort();
        }

        solution intra_route(){
            this.fresh_attr();
            solution res = new solution();
            for(int i=0;i<rout.size();i++){
                res.rout.get(i).clear();
                res.rout.get(i).add(new vertex(0));
                res.rout.get(i).add(new vertex(0));
            }
            for (attr a:this.att
                    ) {
                res.insert(a.route_num,a);
            }
            res.sort();
            res.fresh_attr();
            res.sort();
            return res;
        }

        void insert(int i,attr a){
            int begin=0;
            int end = 0;
            int t=0;
            double temp=Double.MAX_VALUE;
            t = rout.get(i).size();
            for (int j=1;j<t;j++){
                for (int k=j+1;k<t+1;k++){
                    rout.get(i).add(j,new vertex(a.index));
                    rout.get(i).add(k,new vertex(24+a.index));
                    sort();
                    fresh_attr();
                    if (temp>=cost(this)){
                        temp = cost(this);
                        begin = j;
                        end = k;
                    }
                    rout.get(i).remove(k);
                    rout.get(i).remove(j);
                }
            }
            rout.get(i).add(begin,new vertex(a.index));
            rout.get(i).add(end,new vertex(24+a.index));
        }



        public Object clone(){
            solution res = null;
            try {
                res = (solution) super.clone();
            }
            catch (Exception e){
                System.out.print(e.toString());
            }
            Iterator<ArrayList<vertex>> it = rout.iterator();
            res.rout = new ArrayList<ArrayList<vertex>>();
            while (it.hasNext()){
                ArrayList<vertex> tem = new ArrayList<vertex>();
                Iterator<vertex> t = it.next().iterator();
                while(t.hasNext()){
                    tem.add((vertex) t.next().clone());
                }
                res.rout.add(tem);
            }
            res.att = new ArrayList<attr>();
            Iterator<attr> ite = att.iterator();

            while(ite.hasNext()){

                res.att.add((attr) ite.next().clone());
            }

            return res;
        }

        void init_attr(){
            for (int i=0;i<rout.size();i++){
                for (int j=0;j<rout.get(i).size();j++){
                    if(rout.get(i).get(j).index>0&&rout.get(i).get(j).index<=demand_num/2){
                        att.add(new attr(j,rout.get(i).get(j).index,i));
                    }
                }
            }
        }

        void fresh_attr(){
            att.clear();
            for (int i=0;i<rout.size();i++){
                for (int j=0;j<rout.get(i).size();j++){
                    if(rout.get(i).get(j).index>0&&rout.get(i).get(j).index<=demand_num/2){
                        att.add(new attr(j,rout.get(i).get(j).index,i));
                    }
                }
            }
        }


        ArrayList<solution> neighbor(attr a){
            ArrayList<solution> res = new ArrayList<solution>();
            rout.get(a.route_num).remove(a.num);
            num_a = a.route_num;
            for (int i=0;i<rout.get(a.route_num).size();i++){
                if (rout.get(a.route_num).get(i).index==24+a.index){
                    rout.get(a.route_num).remove(i);
                    break;
                }
            }
            for(int i=0;i<rout.size();i++){
                if (i!=a.route_num){
                res.add(insert(a,i));
                }
            }
            return res;
        }

        solution insert(attr a, int i){
            solution res = (solution) this.clone();
            int begin=0;
            int end = 0;
            int t=0;
            double temp=Double.MAX_VALUE;
            t = res.rout.get(i).size();
            for (int j=1;j<t-1;j++){
                for (int k=j+1;k<t;k++){
                    res.rout.get(i).add(j,new vertex(a.index));
                    res.rout.get(i).add(k,new vertex(24+a.index));
                    res.sort();
                    res.fresh_attr();
                    if (temp>=cost(res)){
                        temp = cost(res);
                        begin = j;
                        end = k;
                    }
                    res.rout.get(i).remove(k);
                    res.rout.get(i).remove(j);
                }
            }
            res.rout.get(i).add(begin,new vertex(a.index));
            res.rout.get(i).add(end,new vertex(24+a.index));
            return res;
        }

        double dis(){
            double res=0;
            for (int i=0;i<rout.size();i++){
                for (int j=1;j<rout.get(i).size();j++){
                    res += distance[rout.get(i).get(j).index][rout.get(i).get(j-1).index];
                }
            }
            return res;
        }

        int load(){
            int res=0,l=0;
            for (int i=0;i<rout.size();i++){
                for (int j=0;j<rout.get(i).size();j++){
                    l += demands[rout.get(i).get(j).index].cat;
                    res += max(l-capacity[i],0);
                }
            }
            return res;
        }

        double duration(){
            double res=0;
            for (int i=0;i<rout.size();i++){
                res += max(rout.get(i).get(rout.get(i).size()-1).A-rout.get(i).get(0).D-max_time[i],0);
                }
            return res;
        }

        double window(){
            double res=0;
            for (int i=0;i<rout.size();i++){
                for (int j=0;j<rout.get(i).size();j++){
                    res += max(0,rout.get(i).get(j).B-demands[rout.get(i).get(j).index].late);
                }
            }
            return res;
        }

        double ride(){
            double res=0;
            for (int i=0;i<rout.size();i++){
                for (int j=0;j<rout.get(i).size();j++){
                    if(rout.get(i).get(j).index>0&&rout.get(i).get(j).index<=demand_num/2){
                        res += max(0,rout.get(i).get(j).L-max_travel_time);
                    }
                    res += max(0,rout.get(i).get(j).B-demands[rout.get(i).get(j).index].late);
                }
            }
            return res;
        }

        void sort(int a, int b){
            for (int i=0;i<rout.size();i++){
                if(i==a||i==b) {
                    rout.get(i).get(0).D = demands[0].early;
                    update(i, 0);
                    rout.get(i).get(0).D = demands[0].early + min(F(0, i), W(0, i));
                    update(i, 0);
                    for (int j = 0; j < rout.get(i).size(); j++) {
                        if (rout.get(i).get(j).index > 0 && rout.get(i).get(j).index <= demand_num / 2) {
                            rout.get(i).get(j).B += min(F(j, i), W(j, i));
                            rout.get(i).get(j).D = rout.get(i).get(j).B + demands[rout.get(i).get(j).index].service;
                            update(i, j);
                        }
                    }
                }
            }
        }

        void sort(){
            for (int i=0;i<rout.size();i++){
                rout.get(i).get(0).D = demands[0].early;
                update(i,0);
                rout.get(i).get(0).D = demands[0].early+min(F(0,i),W(0,i));
                update(i,0);
                for (int j=0;j<rout.get(i).size();j++){
                    if (rout.get(i).get(j).index>0&&rout.get(i).get(j).index<=demand_num/2){
                        rout.get(i).get(j).B += min(F(j,i),W(j,i));
                        rout.get(i).get(j).D = rout.get(i).get(j).B + demands[rout.get(i).get(j).index].service;
                        rout.get(i).get(j).L = P(i,j);
                        update(i,j);
                    }
                }
            }
        }
        void update(int i, int b){
            for(int j=b+1;j<rout.get(i).size();j++){
                rout.get(i).get(j).A = rout.get(i).get(j-1).D+distance[rout.get(i).get(j-1).index][rout.get(i).get(j).index];
                rout.get(i).get(j).B = max(demands[rout.get(i).get(j).index].early,rout.get(i).get(j).A);
                rout.get(i).get(j).W = rout.get(i).get(j).B - rout.get(i).get(j).A;
                rout.get(i).get(j).D = rout.get(i).get(j).B + demands[rout.get(i).get(j).index].service;
            }
        }

        double W(int i, int k){
            double res =0;
            for (int j=i+1;j<rout.get(k).size()-1;j++){
                res += rout.get(k).get(j).W;
            }
            return res;
        }

        double F(int i, int k){
            double temp = Double.MAX_VALUE;
            for (int j=i;j<rout.get(k).size();j++){
                double t=0;
                for(int q=i+1;q<j;q++){
                    t += rout.get(k).get(q).W;
                }
                t += max(min(demands[rout.get(k).get(j).index].late-rout.get(k).get(j).B,max_travel_time-P(k,j)),0);
                if (t<=temp){
                    temp = t;
                }
            }
            return temp;
        }

        double P(int a, int b){
            if (rout.get(a).get(b).index<=demand_num/2){
                return 0;
            }
            else{
                double s=0,e=0;
                for(int i=1;i<rout.get(a).size();i++){
                    if(rout.get(a).get(i).index==demands[rout.get(a).get(b).index].partner){
                        s = rout.get(a).get(i).D;
                    }
                }
                e = rout.get(a).get(b).A;
                return e-s;
            }

        }

        double min(double a, double b){
            if(a>=b){
                return b;
            }
            else return a;
        }

        double max(double a, double b){
            if(a>=b){
                return a;
            }
            else return b;
        }


    }

    class demand {
        int index;
        double lon;
        double lat;
        double service;
        int cat;
        double early;
        double late;
        int partner;

        demand(String a, int num) {
            String[] tem;
            tem = a.split("\\s+");
            //System.out.print(tem.length);
            index = Integer.valueOf(tem[1]);
            lon = Double.valueOf(tem[2]);
            lat = Double.valueOf(tem[3]);
            service = Double.valueOf(tem[4]);
            cat = Integer.valueOf(tem[5]);
            early = Double.valueOf(tem[6]);
            late = Double.valueOf(tem[7]);
            if (index <=demand_num/2){
                partner = 24+index;
            }
            else{
                partner = -24+index;
            }

        }

        demand[] init(BufferedReader a) {
            try {
                String s = a.readLine();
                int num = Integer.valueOf(s.split("//s+")[1]);
                demand[] res = new demand[num];
                int i = 0;
                while (s != null) {
                    res[i] = new demand(s, num);
                    i++;
                    s = a.readLine();
                }
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class vertex implements Cloneable{
        int index;
        double A;
        double W;
        double B;
        double D;
        double L;
        vertex(int i){
            index = i;
        }

        public Object clone(){
            vertex res = null;
            try{
                res = (vertex)super.clone();
            }
            catch (Exception e){
                System.out.print(e.toString());
            }
            return res;
        }
    }

    class attr implements Cloneable{
        int num;
        int index;
        int route_num;
        double local_optimal;
        attr(int i,int a, int b){
            num = i;
            index =a;
            route_num =b;
        }
        void setLocal_optimal(double a){
            local_optimal = a;
        }
        public Object clone() {
            attr res = null;
            try {
                 res = (attr)super.clone();
            }
            catch (Exception e){
                System.out.print(e.toString());
            }
            return res;
        }
    }
}