package com.routefinder.exception;

/**
 * Unchecked domain exception thrown when an operation or feature planned for
 * future milestone exploration is invoked before its implementation is complete.
 *
 * <p><b>OOP & Architecture Note:</b>
 * Specialization of {@link UnsupportedOperationException} that explicitly documents
 * planned architectural features (e.g. real-time weather feeds, descent route planning,
 * live GPS tracking) that are scoped for future releases. Catching this allows UI
 * controllers to display informative roadmap notices to users.</p>
 */
public class FeatureNotImplementedException extends UnsupportedOperationException {

    private final String featureName;

    public FeatureNotImplementedException(String featureName) {
        super(String.format("Feature '%s' is not yet implemented and is scheduled for a future release.", featureName));
        this.featureName = featureName;
    }

    public FeatureNotImplementedException(String featureName, String details) {
        super(String.format("Feature '%s' is not yet implemented: %s", featureName, details));
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
}
