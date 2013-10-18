package core;

/**
 *
 * @author mark
 */
public class PagingNavigationSpecular {

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
    public PagingNavigationSpecular(long found, String page, int limit, UrlOption urloption) {
        this.found = found > 999999 ? 999999 : found;

        pagCount = Math.round((this.found + (limit - 1)) / limit); // всего страниц
        //System.out.println((this.found + (limit - 1)) / limit);

        int pageInt = urloption.NumberReplacementInt(page, pagCount);

        this.page = pageInt > 0 ? (pageInt > pagCount ? pagCount : pageInt) : pagCount;

        this.limit = limit;
        this.urloption = urloption;
    }

    public int getPage() {
        return page;
    }

    public String PagingNavigation() {
        int begin = 0; // по куда циклить

        int left = 3; // сколько выводить с лева
        int right = 2; // сколько выводить с права



        // сколько выводить с права
        if (page < left) {
            begin = 0;
        } else {
            begin = page - left;
        }


        // сколько выводить с лева
        if ((page + left) > pagCount) {
            limit = pagCount;
        } else {
            limit = page + right;
        }

        if (begin > 1) {
            //begin = 0;
        }
        if (found <= limit) {
            //limit = 0;
        }




        // ============================
        // вывод навигатора
        // ============================

        String s = "";

        if (page < pagCount && found > 0) {
            s = "<a href=\"?" + urloption.addParam1_1("page=" + (page + 1) + "&q=[delete]") + "\">« Сюда</a>";
        }

        //if (page < (pagCount-left)) {
        //    s += "<a href=\"?" + urloption.addParam("page="+pagCount) + "\">"+pagCount+"</a>";
        //}

        if (page < (pagCount - left) + 1) {
            s += "<a href=\"?" + urloption.addParam1_1("page=" + ((limit != 1 ? limit : 0) + 1) + "&q=[delete]") + "\">...</a>";
        }



        for (int i = (int) (limit != 1 ? limit : 0); i > begin; i--) {
            if (limit == 0) {
                break;
            }
            if (i == page) {
                s += "<b>" + i + "</b>";
            } else {
                s += "<a href=\"?" + urloption.addParam1_1("page=" + i + "&q=[delete]") + "\">" + i + "</a>";
            }
        }

        if (page > right + 1) {
            s += "<span><a href=\"?" + urloption.addParam1_1("page=" + (begin) + "&q=[delete]") + "\">...</a></span>";
        }

        if (page > 1) {
            s += "<a href=\"?" + urloption.addParam1_1("page=" + (page - 1) + "&q=[delete]") + "\">Туда »</a>";
        }

        return s;
    }

}