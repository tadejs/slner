package si.ijs.slner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Scores extends LinkedHashMap<String, List<Double>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5817255912977372386L;

	public void addAll(Scores m) {
		for (String key : m.keySet()) {
			List<Double> d = get(key);
			List<Double> d2 = m.get(key);
			if (d == null) {
				put(key, d2);
			} else {
				d.addAll(d2);
			}
		}
	}

	public void add(String key, double val) {
		List<Double> lst = get(key);
		if (lst == null) {
			lst = new ArrayList<Double>();
			put(key, lst);
		}
		lst.add(val);
	}
	
	public Map<String, Double> avg() {
		Map<String, Double> m = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, List<Double>> me : entrySet()) {
			double sum = 0;
			for (Double d : me.getValue()) {
				sum += d;
			}
			m.put(me.getKey(), sum / me.getValue().size());
		}
		return m;
	}
}