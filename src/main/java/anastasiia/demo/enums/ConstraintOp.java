package anastasiia.demo.enums;

import org.ojalgo.optimisation.Expression;

public enum ConstraintOp {
    EQUALS {
        @Override
        public void applyTo(Expression expr, double value, double tolerance) {
            expr.lower(value - tolerance);
            expr.upper(value + tolerance);
        }
    },
    LESS_THAN {
        @Override
        public void applyTo(Expression expr, double value, double tolerance) {
            expr.upper(value - EPSILON + tolerance);
        }
    },
    LESS_THAN_OR_EQUAL {
        @Override
        public void applyTo(Expression expr, double value, double tolerance) {
            expr.upper(value + tolerance);
        }
    },
    GREATER_THAN {
        @Override
        public void applyTo(Expression expr, double value, double tolerance) {
            expr.lower(value + EPSILON - tolerance);
        }
    },
    GREATER_THAN_OR_EQUAL {
        @Override
        public void applyTo(Expression expr, double value, double tolerance) {
            expr.lower(value - tolerance);
        }
    };

    private static final double EPSILON = 1e-6;

    public abstract void applyTo(Expression expr, double value, double tolerance);
}
