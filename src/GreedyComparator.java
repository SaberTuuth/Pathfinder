import java.util.Comparator;

public class GreedyComparator implements Comparator<PathSearch.PathNode> {

    @Override
    public int compare(PathSearch.PathNode lhn, PathSearch.PathNode rhn){
        if(PathSearch.Search.equals(PathSearch.SearchMethod.GREEDY)){
            return Integer.compare(lhn.heuristicCost, rhn.heuristicCost);
        }else{
            return 1;
        }
    }
}
