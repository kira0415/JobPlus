package recommendation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import database.MySQLConnection;
import entity.Item;
import external.GitHubClient;
/**
 * This class got the recommending items from github job based on user favorite items
 * @author Kira
 *
 */
public class Recommendation {
	/**
	 * Read keywords of user favorite items. Search items based on said keywords
	 * @param userId - current user
	 * @param lat - user latitude
	 * @param lon - user longitude
	 * @return
	 */
	public List<Item> recommendation(String userId, double lat, double lon) {
		List<Item> recommendedItems = new ArrayList<>();
		
		MySQLConnection connection = new MySQLConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		
		Map<String, Integer> allKeywords = new HashMap<>();
		for(String itemId : favoritedItemIds) {
			Set<String> keywords = connection.getKeywords(itemId);
			for(String keyword : keywords) {
				allKeywords.put(keyword, allKeywords.getOrDefault(keyword, 0) + 1);
			}
		}
		connection.close();
		
		List<Entry<String, Integer>> keywordList = new ArrayList<>(allKeywords.entrySet());
		Collections.sort(keywordList, (Entry<String, Integer> e1, Entry<String, Integer> e2) -> {
			return Integer.compare(e2.getValue(), e1.getValue());
		});
		
		if(keywordList.size() > 3) {
			keywordList = keywordList.subList(0, 3);
		}
		
		Set<String> visitedItemIds = new HashSet<>();
		GitHubClient client = new GitHubClient();
		
		for(Entry<String, Integer> keyword : keywordList) {
			List<Item> items = client.search(lat, lon, keyword.getKey());
			
			for(Item item : items) {
				recommendedItems.add(item);
				/*if(!favoritedItemIds.contains(item.getItemId()) && 
					!visitedItemIds.contains(item.getItemId())) {
					recommendedItems.add(item);
					visitedItemIds.add(item.getItemId());
				}*/
			}
		}
		
		return recommendedItems;
	}
}
