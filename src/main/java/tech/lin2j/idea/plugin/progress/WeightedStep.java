package tech.lin2j.idea.plugin.progress;

/**
 * @author linjinjia
 * @date 2024/11/17 15:50
 */
public interface WeightedStep {

    float getWeight();

    WeightedStep nextStep();

    boolean isFinal();
}