import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.*;


public class SudokuSolver {

    //revise function that takes sudoku.json file as csp and 2 variables and modifies the csp
    @SuppressWarnings("unchecked")
    public static boolean revise(String fileName, String var1, String var2) {
        boolean removedValues = false;
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray cspJson = (JSONArray) obj;
            ArrayList<HashMap<String, Object>> csp = new ArrayList<HashMap<String, Object>>();
            for (Object o : cspJson) {
                JSONArray constraint = (JSONArray) o;
                HashMap<String, Object> constraintMap = new HashMap<String, Object>();
                constraintMap.put(constraint.get(0).toString(), (JSONArray) constraint.get(1));
                csp.add(constraintMap);
            }
            ArrayList<Integer> domain1 = null;
            ArrayList<Integer> domain2 = null;
            for (HashMap<String, Object> constraint : csp) {
                if (constraint.containsKey(var1)) {
                    domain1 = (ArrayList<Integer>) constraint.get(var1);
                } else if (constraint.containsKey(var2)) {
                    domain2 = (ArrayList<Integer>) constraint.get(var2);
                }
                if (domain1 != null && domain2 != null) {
                    for (int value : new ArrayList<Integer>(domain1)) {
                        boolean satisfiesConstraint = false;
                        for (int otherValue : domain2) {
                            if (satisfiesConstraint(var1, var2, value, otherValue, csp)) {
                                satisfiesConstraint = true;
                                break;
                            }
                        }
                        if (!satisfiesConstraint) {
                            domain1.remove((Integer) value);
                            removedValues = true;
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return removedValues;
    }

    //helper function for revise
    private static boolean satisfiesConstraint(String var1, String var2, int value1, int value2,
                                               ArrayList<HashMap<String, Object>> csp) {
        for (HashMap<String, Object> constraint : csp) {
            if (constraint.containsKey(var1) && constraint.containsKey(var2)) {
                if (constraint.get(var1) instanceof JSONArray && constraint.get(var2) instanceof JSONArray) {
                    JSONArray values1 = (JSONArray) constraint.get(var1);
                    JSONArray values2 = (JSONArray) constraint.get(var2);
                    if ((int) values1.get(0) == value1 && (int) values2.get(0) == value2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //ac3 function that takes sudoku.json file as csp and removes inconsistent values across domains
    public static boolean ac3(String fileName) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray cspJson = (JSONArray) obj;
            ArrayList<HashMap<String, Object>> csp = new ArrayList<HashMap<String, Object>>();
            for (Object o : cspJson) {
                JSONArray constraint = (JSONArray) o;
                HashMap<String, Object> constraintMap = new HashMap<String, Object>();
                constraintMap.put(constraint.get(0).toString(), (JSONArray) constraint.get(1));
                csp.add(constraintMap);
            }

            // Initialize the queue with all arcs in the CSP
            Queue<String[]> queue = new LinkedList<String[]>();
            for (HashMap<String, Object> constraint : csp) {
                for (String var1 : constraint.keySet()) {
                    for (String var2 : constraint.keySet()) {
                        if (!var1.equals(var2)) {
                            queue.add(new String[] { var1, var2 });
                        }
                    }
                }
            }

            // Process the queue until it is empty
            while (!queue.isEmpty()) {
                String[] arc = queue.remove();
                boolean removedValues = revise(fileName, arc[0], arc[1]);
                if (removedValues) {
                    ArrayList<Integer> domain1 = getDomain(fileName, arc[0]);
                    if (domain1.size() == 0) {
                        return false;
                    }
                    for (HashMap<String, Object> constraint : csp) {
                        if (constraint.containsKey(arc[0]) && !constraint.containsKey(arc[1])) {
                            for (String var2 : constraint.keySet()) {
                                if (!var2.equals(arc[0])) {
                                    queue.add(new String[] { var2, arc[0] });
                                }
                            }
                        }
                    }
                }
            }

            // Check if all variables have at least one value left in their domains
            for (HashMap<String, Object> constraint : csp) {
                String var = constraint.keySet().iterator().next();
                ArrayList<Integer> domain = (ArrayList<Integer>) constraint.get(var);
                if (domain.size() == 0) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //helper function for ac3
    public static ArrayList<Integer> getDomain(String fileName, String variable) {
        ArrayList<Integer> domain = new ArrayList<Integer>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray cspJson = (JSONArray) obj;
            for (Object o : cspJson) {
                JSONArray constraint = (JSONArray) o;
                String var = constraint.get(0).toString();
                if (var.equals(variable)) {
                    JSONArray values = (JSONArray) constraint.get(1);
                    for (Object value : values) {
                        domain.add(Integer.parseInt(value.toString()));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domain;
    }

    // minimumRemainingValues function that takes sudoku.json file as csp and set of variable assignments as input and returns variable with fewest values in the domain
    public static String minimumRemainingValues(String fileName, Set<String> assignments) {
        JSONParser parser = new JSONParser();
        HashMap<String, ArrayList<Integer>> domains = new HashMap<String, ArrayList<Integer>>();

        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray cspJson = (JSONArray) obj;
            for (Object o : cspJson) {
                JSONArray constraint = (JSONArray) o;
                String var = constraint.get(0).toString();
                if (!assignments.contains(var)) {
                    ArrayList<Integer> domain = new ArrayList<Integer>();
                    JSONArray values = (JSONArray) constraint.get(1);
                    for (Object value : values) {
                        domain.add(Integer.parseInt(value.toString()));
                    }
                    domains.put(var, domain);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String minVar = null;
        int minDomainSize = Integer.MAX_VALUE;
        for (String var : domains.keySet()) {
            ArrayList<Integer> domain = domains.get(var);
            if (domain.size() < minDomainSize) {
                minVar = var;
                minDomainSize = domain.size();
            }
        }

        return minVar;
    }

    //backtrackingsearch function utilizing ac3 to maintain arc consistency and minimumRemainingValues while choosing variable to assign
    public static HashMap<String, Object> backtrackingsearch(String fileName) {
        JSONParser parser = new JSONParser();
        ArrayList<HashMap<String, Object>> csp = new ArrayList<HashMap<String, Object>>();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONArray cspJson = (JSONArray) obj;
            for (Object o : cspJson) {
                JSONArray constraint = (JSONArray) o;
                HashMap<String, Object> constraintMap = new HashMap<String, Object>();
                constraintMap.put(constraint.get(0).toString(), (JSONArray) constraint.get(1));
                csp.add(constraintMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, Integer> assignment = new HashMap<String, Integer>();
        ArrayList<String> assignedVariables = new ArrayList<String>();
        ArrayList<ArrayList<Integer>> remainingDomains = new ArrayList<ArrayList<Integer>>();
        HashMap<String, HashSet<Integer>> failedValues = new HashMap<String, HashSet<Integer>>(); // new data structure to keep track of failed values
        HashMap<String, Integer> backtrackCounts = new HashMap<String, Integer>(); // new data structure to keep track of the number of backtracks for each variable
        boolean success = backtrack(assignment, assignedVariables, remainingDomains, csp, fileName, failedValues, backtrackCounts);

        if (success) {
            System.out.println("Solution found:");
            System.out.println(assignment);
        } else {
            System.out.println("No solution found.");
        }

        // Construct and return the result hashmap
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("assignment", assignment);
        result.put("assignedVariables", assignedVariables);
        result.put("remainingDomains", remainingDomains);
        result.put("failedValues", failedValues); // add failedValues to the result hashmap
        result.put("backtrackCounts", backtrackCounts); // add backtrackCounts to the result hashmap
        return result;
    }

    //helper function for backtrackingSearch keeps track of failed values and backtrack counts
    private static boolean backtrack(HashMap<String, Integer> assignment, ArrayList<String> assignedVariables,
                                     ArrayList<ArrayList<Integer>> remainingDomains, ArrayList<HashMap<String, Object>> csp,
                                     String fileName, HashMap<String, HashSet<Integer>> failedValues,
                                     HashMap<String, Integer> backtrackCounts) {
        // Check if all variables have been assigned a value
        if (assignment.size() == csp.size()) {
            return true;
        }

        // Choose the variable to assign using minimum remaining values heuristic
        String var = minimumRemainingValues(fileName, assignment.keySet());

        // Get the domain of the selected variable
        ArrayList<Integer> domain = getDomain(fileName, var);
        ArrayList<Integer> remainingDomain = new ArrayList<Integer>(domain);
        remainingDomains.add(remainingDomain);

        // Sort the domain of the selected variable in ascending order of the number of times each value has failed
        //ArrayList<Integer> sortedDomain = new ArrayList<Integer>(domain);
/*        sortedDomain.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int count1 = failedValues.get(var).contains(o1) ? backtrackCounts.getOrDefault(var, 0) + 1 : 0;
                int count2 = failedValues.get(var).contains(o2) ? backtrackCounts.getOrDefault(var, 0) + 1 : 0;
                return Integer.compare(count1, count2);
            }
        });*/
        ArrayList<Integer> sortedDomain = new ArrayList<Integer>(domain);
        if (failedValues.get(var) != null && backtrackCounts.get(var) != null) {
            sortedDomain.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    int count1 = failedValues.get(var).contains(o1) ? backtrackCounts.get(var) + 1 : 0;
                    int count2 = failedValues.get(var).contains(o2) ? backtrackCounts.get(var) + 1 : 0;
                    return count1 - count2;
                }
            });
        } else {
            sortedDomain.sort(null); // use the natural ordering of the integers
        }

        // Iterate over the domain of the selected variable in the sorted order
        for (int value : sortedDomain) {
            // Check if the value is consistent with the current assignment
            assignment.put(var, value);
            boolean consistent = ac3(fileName);
            if (consistent) {
                assignedVariables.add(var);

                // Recurse on the next variable
                boolean result = backtrack(assignment, assignedVariables, remainingDomains, csp, fileName, failedValues, backtrackCounts);
                if (result) {
                    return true;
                }

                // Backtrack and remove the assigned variable from the assignment
                assignedVariables.remove(assignedVariables.size() - 1);
                failedValues.get(var).add(value);
                backtrackCounts.put(var, backtrackCounts.getOrDefault(var, 0) + 1);
            }
            assignment.remove(var);
            remainingDomain.remove(new Integer(value));
        }
        return false;
    }



    // helper function to check if it is safe to insert a number in a sudoku grid
    public boolean isSafe(int[][] b, int r, int c, int n)
    {
      // the loop takes care of the clash in the row of the grid
        for (int d = 0; d < b.length; d++)
        {

      // if the number that we have inserted is already
      // present in that row then return false
            if (b[r][d] == n)
            {
                return false;
            }
        }

     // the loop takes care of the clash in the column of the grid
        for (int r1 = 0; r1 < b.length; r1++)
        {  // if the number that we have inserted is already
     // present in that column then return false
            if (b[r1][c] == n)
            {
                return false;
            }
        }

      // the loop takes care of the clash in the sub-grid that is present in the grid
        int sqt = (int)Math.sqrt(b.length);
        int boxRowSt = r - r % sqt;
        int boxColSt = c - c % sqt;

        for (int r1 = boxRowSt; r1 < boxRowSt + sqt; r1++)
        {
            for (int d = boxColSt; d < boxColSt + sqt; d++)
            {
               // if the number that we have inserted is already
                // present in that sub-grid then return false
                if (b[r1][d] == n)
                {
                    return false;
                }
            }
        }

   // if there is no clash in the grid, then it is safe and
   // true is returned
        return true;
    }

    //the helper function used to parse the 2D array input puzzle
    public boolean parseSudoku(int[][] b, int num)
    {
        int r = -1;
        int c = -1;
        boolean isVacant = true;
        for (int i = 0; i < num; i++)
        {
            for (int j = 0; j < num; j++)
            {  if (b[i][j] == 0)
            {
                r = i;
                c = j;

                // false value means
                // there is still some
                // vacant cells in the grid
                isVacant = false;
                break;
            }
            }

            if (!isVacant)
            {
                break;
            }
        }

         // there is no empty space left in the grid

        if (isVacant)
        {
            return true;
        }


        for (int no = 1; no <= num; no++)
        {
            if (isSafe(b, r, c, no))
            {
                b[r][c] = no;
                if (parseSudoku(b, num))
                {
         // display(board, num);
                    return true;
                }
                else
                {

                    b[r][c] = 0;
                }
            }
        }
        return false;
    }











}

