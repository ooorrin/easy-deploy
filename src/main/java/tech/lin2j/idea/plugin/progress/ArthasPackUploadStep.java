package tech.lin2j.idea.plugin.progress;


import com.intellij.openapi.components.ServiceManager;
import tech.lin2j.idea.plugin.service.IHotReloadService;

/**
 * @author linjinjia
 * @date 2024/11/17 16:14
 */
public enum ArthasPackUploadStep implements WeightedStep {

    START(0) {
        @Override
        public WeightedStep nextStep() {

            return null;
        }
    },

    CHECK_PACK_EXIST(0.2f) {
        @Override
        public WeightedStep nextStep() {
            return EXTRACT_TO_LOCAL;
        }
    },

    EXTRACT_TO_LOCAL(0.2f) {
        @Override
        public WeightedStep nextStep() {
            return UPLOAD_TO_REMOTE;
        }
    },

    UPLOAD_TO_REMOTE(0.4f) {
        @Override
        public WeightedStep nextStep() {
            return null;
        }
    },

    EXTRACT_TO_REMOTE(0.2f) {
        @Override
        public WeightedStep nextStep() {
            return null;
        }

        @Override
        public boolean isFinal() {
            return true;
        }
    }

    ;

    private final float weight;

    ArthasPackUploadStep(float weight) {
        this.weight = weight;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    private IHotReloadService getHotReloadService() {
        return ServiceManager.getService(IHotReloadService.class);
    };
}