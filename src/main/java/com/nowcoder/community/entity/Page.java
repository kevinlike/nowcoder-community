package com.nowcoder.community.entity;


/*
 * 封装分页相关信息
 */
public class Page {
    //current page number
    private int current=1;
    //how many items in one page
    private int limit=10;

    //总的信息条数
    private int rows;
    //查询路径，用于分页连接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current>=1)
        {
            this.current = current;
        }
        
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=100)
        {
            this.limit = limit;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0)
        {
            this.rows = rows;
        }
        
    }
    //获取当前页的起始行
    public int getOffset(){
        return (current-1)*limit;
    }
    //获取总页数
    public int getTotal(){
        return rows%limit==0?rows/limit:(rows/limit+1);
    }

    //获取选页栏起始页码
    public int getFrom(){
        int from=current-2;
        return from<1?1:from;
    }

    //获取选页栏结束页码
    public int getTo(){
        int to=current+2;
        int total=getTotal();
        return to>total?total:to;
    }

}
