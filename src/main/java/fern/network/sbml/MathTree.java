package fern.network.sbml;

import fern.network.AmountManager;
import fern.simulation.Simulator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.sbml.jsbml.ASTNode;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;

/**
 * Representation of am evaluation tree. Within a sbml file, MathML branches may occur at different
 * positions. These are represented as MathTrees in FERN.
 *
 * @author Florian Erhard
 */
public class MathTree {

  private ASTNode copiedAST;
  private SBMLinterpreter sbmlInterpreter;
  public static final String TEMP_VALUE = "SBML_SIMULATION_TEMP_VALUE";

  /**
   * Creates a MathTree {@link ASTNode}.
   *
   * @param interpreter sbmlInterpreter instance for calculating the nodes
   * @param ast         ASTNode
   */
  public MathTree(SBMLinterpreter interpreter, ASTNode ast) {
    sbmlInterpreter = interpreter;
    copiedAST = interpreter.copyAST(ast, true, null, null);
  }

  /**
   * Gets the species present in this tree.
   *
   * @return indices of the species.
   */
  public List<Integer> getSpecies() {
    List<Integer> re = new LinkedList<>();
    Stack<ASTNode> dfs = new Stack<>();
    dfs.add(copiedAST);
    while (!dfs.empty()) {
      ASTNode node = dfs.pop();
      if ((node.getNumChildren() == 0) && !node.isOperator() && !node.isNumber()) {
        Integer index = null;
        if (sbmlInterpreter.getModel().getSpecies(node.getName()) != null) {
          // Subtracting from the total compartment count as species indices start after compartments
          // in the Y array in the interpreter.
          index = sbmlInterpreter.getSymbolHash().get(node.getName()) - sbmlInterpreter.getModel()
              .getCompartmentCount();
        }
        if ((index != null) && !re.contains(index)) {
          re.add(index);
        }
      } else if (node.getNumChildren() != 0) {
        for (int i = 0; i < node.getNumChildren(); i++) {
          dfs.add(node.getChild(i));
        }
      }
    }
    return re;
  }

  /**
   * Gets the ASTNode of this MathTree.
   *
   * @return the copiedAST of this MathTree
   */
  public ASTNode getCopiedAST() {
    return copiedAST;
  }

  /**
   * Evaluate the MathTree.
   *
   * @param amount AmountManager
   * @param sim    Simulator
   * @return value of the expression
   */
  public double calculate(AmountManager amount, Simulator sim) {
    sbmlInterpreter.updateSpeciesConcentration(amount);
    sbmlInterpreter.setCurrentTime(sim.getTime());
    return ((ASTNodeValue) copiedAST.getUserObject(TEMP_VALUE)).compileDouble(sim.getTime(), 0d);
  }

}
