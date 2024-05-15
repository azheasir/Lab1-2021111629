import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Lab1 {
    private Map<String, Map<String, Integer>> graph;
	private Map<String, Integer> node;
    public Lab1() {
        this.graph = new HashMap<>();
		this.node = new HashMap<>();
    }
	public void buildGraphFromFile(String filePath) throws FileNotFoundException {
		List<String> words = new ArrayList<>();
        Scanner scanner = new Scanner(new java.io.File(filePath));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
			line = line.toLowerCase();
			String word = new String();
            for (int i = 0; i < line.length(); i++) {
				if (Character.isLetter(line.charAt(i))) {
					word = word + line.charAt(i);
				} else {
					if (word.length() > 0) {
						words.add(word);
						word = "";
					}
				}
			}
			if (word.length() > 0) {
				words.add(word);
				word = "";
			}	
		}
		for (int i = 0; i < words.size(); i++) {
			// System.out.println(words.get(i));
			if (node.containsKey(words.get(i)) == false) {
				node.put(words.get(i), 1);
			}
		}
		for (int i = 1; i < words.size(); i++) {
			String word1 = words.get(i - 1);
			String word2 = words.get(i);
			if (graph.containsKey(word1)) {
				Map<String, Integer> tmp = graph.get(word1);
				if (tmp.containsKey(word2)) {
					tmp.put(word2, tmp.get(word2) + 1);
				} else {
					tmp.put(word2,1);
				}
				graph.put(word1, tmp);
			} else {
				Map<String, Integer> tmp = new HashMap<String, Integer>();
				tmp.put(word2, 1);
				graph.put(word1, tmp);
			}
		}
		for (String word : node.keySet()) {
			if (graph.containsKey(word) == false) {
				graph.put(word, new HashMap<String, Integer>());
			}
		}
		// for (String word : graph.keySet()) {
		// 	for (String word2 : graph.get(word).keySet()) {
		// 		System.out.println(word + "---" + graph.get(word).get(word2) + "--->" + word2);
		// 	}
		// }
		scanner.close();
	}


	public static void main(String[] args) {
        Lab1 lab1 = new Lab1();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input file path: ");
        String filePath = scanner.nextLine();
        try {
            lab1.buildGraphFromFile(filePath);
		} catch (FileNotFoundException e) {
            System.err.println("File not found: " + filePath);
        }
		while (true) {
			System.out.println("Please input choice");
			System.out.println("1. show the directed graph");
			System.out.println("2. query bridge words");
			System.out.println("3. generate new text");
			System.out.println("4. calculate shortest path");
			System.out.println("5. random walk");
			System.out.println("0. exit");

			int choice = scanner.nextInt();
			scanner.nextLine();

			switch (choice) {
				case 1:
					lab1.showDirectedGraph(new HashMap<>());
					break;
				case 2:
					System.out.println("Please input two words");
					String word1 = scanner.next();
					String word2 = scanner.next();
					lab1.queryBridgeWords(word1, word2);
					break;
				case 3:
					System.out.println("Please input a line of text");
					String inputText = scanner.nextLine();
					String newText = lab1.generateNewText(inputText);
					System.out.println(newText);
					break;
				case 4:
					System.out.println("Please input source and destination word");
					String startWord = scanner.next();
					String endWord = scanner.next();
					lab1.calcShortestPath(startWord, endWord);
					break;
				case 5:
					lab1.randomWalk();
					break;
				case 0:
					return;
				default:
					System.out.println("Invalid!");
			}
		}
    }


	public void showDirectedGraph(Map<String, Map<String, Integer>> highlight) {
		StringBuilder newText = new StringBuilder();
		newText.append("digraph G {\n");
		for (String source : graph.keySet()) {
			Map<String, Integer> edges = graph.get(source);
			for (String target : edges.keySet()) {
				int weight = edges.get(target);
				String color = "black";
				if(highlight.containsKey(source) && highlight.get(source).containsKey(target)) {
					color = "red";
				}
				newText.append(String.format("  \"%s\" -> \"%s\" [label=\"%d\", color=\"%s\"]\n", source, target, weight, color));
			}
		}

		newText.append("}\n");

		try {
            String filePath = "output";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(newText.toString());
            writer.close();
            System.out.println("Write to " + filePath + " success.");
        } catch (IOException e) {
            e.printStackTrace();
        }
		try {
            Process process = Runtime.getRuntime().exec("cmd /c dot ./output -Tsvg > a.svg");
            int exitCode = process.waitFor();
            System.out.println("Generate exit code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

	public String queryBridgeWords(String word1, String word2) {
		return internal_queryBridgeWords(word1, word2, false);
	}
	private String internal_queryBridgeWords(String word1, String word2, Boolean quiet) {
		if (node.containsKey(word1) == false || node.containsKey(word2) == false) {
			if (quiet == false) {
				System.out.println("No word1 or word2 in the graph!");
			}
			return null;
		}
		List<String> ans = new ArrayList<>();
		for (String word : node.keySet()) {
			if (graph.get(word1).containsKey(word) && graph.containsKey(word) && graph.get(word).containsKey(word2)) {
				ans.add(word);
			}
		}
		if (ans.size() == 0) {
			if (quiet == false) {
				System.out.println("No bridge words from word1 to word2!");
			}
			return null;
		} else {
			StringBuilder newText = new StringBuilder();
			newText.append("The bridge words from word1 to word2 are: ");
			for (int i = 0; i < ans.size(); i++) {
				newText.append(ans.get(i));
				if (i != ans.size() - 1) {
					newText.append(",");
					if (i == ans.size() - 2) {
						newText.append("and ");
					}
				} else {
					newText.append(".");
				}
			}
			if (quiet == false) {
				System.out.println(newText.toString());
			}
			return ans.get(0);
		}
    }


	public String generateNewText(String inputText) {
		String[] words = inputText.split(" ");
        StringBuilder newText = new StringBuilder();
        
        for (int i = 0; i < words.length - 1; i++) {
            String currentWord = words[i];
            String nextWord = words[i + 1];
            newText.append(currentWord).append(" ");
            String bridgeWord = internal_queryBridgeWords(currentWord, nextWord, true);
            if (bridgeWord != null && !bridgeWord.isEmpty()) {
                newText.append(bridgeWord).append(" ");
            }
        }
        newText.append(words[words.length - 1]);
        return newText.toString();
    }


	public String calcShortestPath(String word1, String word2) {
		Map<String, Integer> dis = new HashMap<>();
		Map<String, String> pre = new HashMap<>();
		Map<String, Boolean> vis = new HashMap<>();
		for (String word : node.keySet()) {
			dis.put(word, Integer.MAX_VALUE);
			vis.put(word, false);
		}
		dis.put(word1, 0);
		while (true) {
			String now = null;
			for (String word : node.keySet()) {
				if (vis.get(word) == true) {
					continue;
				}
				if (now == null || dis.get(word) < dis.get(now)) {
					now = word;
				}
			}
			if (now.equals(word2)) {
				break;
			}
			vis.put(now, true);
			for (String nextWord : graph.get(now).keySet()) {
				int weight = graph.get(now).get(nextWord);
				if (dis.get(nextWord) > dis.get(now) + weight) {
					dis.put(nextWord, dis.get(now) + weight);
					pre.put(nextWord, now);
				}
			}
		}
		if (dis.get(word2) == Integer.MAX_VALUE) {
			System.out.println("Word2 unreachable from word1!");
			return null;
		} else {
			System.out.println("Shortest Path: " + dis.get(word2));
			Map<String, Map<String, Integer>> highlight = new HashMap<>();
			String now = word2;
			while (pre.containsKey(now)) {
				String tmp = pre.get(now);
				if (highlight.containsKey(tmp) == false) {
					highlight.put(tmp, new HashMap<>());
				}
				if (highlight.get(tmp).containsKey(now) == false) {
					highlight.get(tmp).put(now, 1);
				}
				now = tmp;
			}
			showDirectedGraph(highlight);
			return null;
		}
    }

	private static Map<String, Map<String, Integer>> deepCopyGraph(Map<String, Map<String, Integer>> original) {
        Map<String, Map<String, Integer>> copy = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> entry : original.entrySet()) {
            String node = entry.getKey();
            Map<String, Integer> neighbors = entry.getValue();
            Map<String, Integer> neighborsCopy = new HashMap<>();

            for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
                String neighbor = neighborEntry.getKey();
                int weight = neighborEntry.getValue();
                neighborsCopy.put(neighbor, weight);
            }

            copy.put(node, neighborsCopy);
        }

        return copy;
    }
	public String randomWalk() {
		Map<String, Map<String, Integer>> g = deepCopyGraph(graph);
		Random random = new Random();
        List<String> tmp = new ArrayList<>(node.keySet());
		String now = tmp.get(random.nextInt(tmp.size()));
		while (true) {
			System.out.print(now + " ");
			if (g.get(now).size() == 0) {
				break;
			}
			List<String> cand = new ArrayList<>(g.get(now).keySet());
			String next = cand.get(random.nextInt(cand.size()));
			g.get(now).remove(next);
			now = next;
		}
		System.out.println();
        return null;
    }
}
