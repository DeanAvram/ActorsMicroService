package cloud.actorsmicroservice.boundaries;

public class ActorSearchBoundray {
    private String criteria;
    private String value;

    public ActorSearchBoundray (){
    };

    public ActorSearchBoundray(String criteria, String value){
        this.criteria = criteria;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    @Override
    public String toString() {
        return "ActorSearchBoundray{" +
                "criteria='" + criteria + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
