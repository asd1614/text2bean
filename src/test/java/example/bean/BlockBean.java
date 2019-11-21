package example.bean;

public class BlockBean {

    private String title;

    private String status;

    private String category;

    private String balance;

    private String remain;

    private String other1;

    private String other2;

    private String other3;

    private String other4;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getRemain() {
        return remain;
    }

    public void setRemain(String remain) {
        this.remain = remain;
    }

    public String getOther1() {
        return other1;
    }

    public void setOther1(String other1) {
        this.other1 = other1;
    }

    public String getOther2() {
        return other2;
    }

    public void setOther2(String other2) {
        this.other2 = other2;
    }

    public String getOther3() {
        return other3;
    }

    public void setOther3(String other3) {
        this.other3 = other3;
    }

    public String getOther4() {
        return other4;
    }

    public void setOther4(String other4) {
        this.other4 = other4;
    }

    @Override
    public String toString() {
        return String.format("{title: %s, status: %s, category: %s, balance: %s, remain: %s, other1: %s, other2: %s, other3: %s, other4: %s}",
                title, status, category, balance, remain, other1, other2, other3, other4);
    }
}
