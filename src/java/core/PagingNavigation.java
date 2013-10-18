package core;

/**
 *
 * @author mark
 */
public class PagingNavigation {

    private UrlOption urloption;
    private long found, limit;
    private int page, pagCount;

    /**
     * 
     * @param found
     * @param page
     * @param limit
     * @param urloption 
     */
    public PagingNavigation(long found, String page, int limit, UrlOption urloption) {
        this.found = found > 1000 ? 1000 : found;
        
        int pageInt = urloption.NumberReplacementInt(page, pagCount);
        pagCount = (int)((this.found + (limit - 1)) / limit); // всего страниц
        this.page = pageInt > 0 ? (pageInt > pagCount ? pagCount : pageInt) : 1;
        
        this.limit = limit;
        this.urloption = urloption;
    }
    
    public int getPage() {
        return page;
    }

    public String PagingNavigation() {
        int begin = 0; // начало цикла

        int left = 5; // сколько выводить с лева
        int right = 6; // сколько выводить с права


        // сколько выводить с лева
        if (page > left) {
            begin = page - left;
        } else {
            begin = 1;
        }

        // сколько выводить с права
        if ((page + right) <= pagCount) {
            limit = page + right;
        } else {
            limit = pagCount;
        }

        if (begin < 1) {
            begin = 0;
        }
        if (found <= limit) {
            limit = 0;
        }

        // ============================
        // вывод навигатора
        // ============================

        String s = "";

        if (page > 1 && found > 0) {
            s = "<a href=\"?" + urloption.addParam("page=" + (page - 1)) + "\">« сюда</a>";
        }

        if (page > left + 2) {
            s += "<a href=\"?" + urloption.addParam("page=1") + "\">1</a>";
        }
        if (page > left + 1) {
            s += "<a href=\"?" + urloption.addParam("page=" + (begin - 1)) + "\">...</a>";
        }

        for (int i = 0; i >= begin; i--) {
            if (limit == 0) {
                break;
            }
            if (i == page) {
                s += "<b>" + i + "</b>";
            } else {
                s += "<a href=\"?" + urloption.addParam("page=" + i) + "\">" + i + "</a>";
            }
        }

        if (page < pagCount) {
            if (limit != pagCount) {
                s += "<span><a href=\"?" + urloption.addParam("page=" + (limit + 1)) + "\">...</a></span>";
            }
            s += "<a href=\"?" + urloption.addParam("page=" + (page + 1)) + "\">туда »</a>";
        }

        return s;
    }
    
    public String PagingPreviousNext() {
        System.out.println(page);
        String s = "";
        if (page > 1) {
            s += "<a href=\"?" + urloption.addParam1_1("page=" + (page - 1)) + "\">« Сюда</a>";
        }

        if (page < pagCount) {
            s += "<a href=\"?" + urloption.addParam1_1("page=" + (page + 1)) + "\">Туда »</a>";
        }

        return s;
    }
}