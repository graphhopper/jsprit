package com.graphhopper.jsprit.core.reporting;

import java.util.function.Consumer;

import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition;
import com.graphhopper.jsprit.core.reporting.columndefinition.ColumnDefinition.Builder;

/**
 * Abstract base class for column definitions.
 *
 * @author balage
 *
 * @param <C>
 *            The context the column works on
 * @param <T>
 *            The type of the data it emits
 * @param <A>
 *            The class itself (internal generic parameter: for inheritence and
 *            builder pattern)
 */
public abstract class AbstractPrinterColumn<C extends PrinterContext, T, A extends AbstractPrinterColumn<C, T, A>> {

    // Decorator is a post creation callback to alter the behaviour of the
    // column definition.
    private Consumer<ColumnDefinition.Builder> decorator;

    private String title;

    /**
     * Constructor.
     */
    public AbstractPrinterColumn() {
        this(null);
    }

    /**
     * @param decorator
     *            Decorator is a post creation callback to alter the behaviour
     *            of the column definition.
     */
    public AbstractPrinterColumn(Consumer<ColumnDefinition.Builder> decorator) {
        super();
        this.decorator = decorator;
        this.title = getDefaultTitle();
    }

    /**
     * Creates the column definition of the column.
     *
     * @return the decorated column definition.
     */
    public ColumnDefinition getColumnDefinition() {
        Builder builder = getColumnBuilder().withTitle(getTitle());
        if (decorator != null) {
            decorator.accept(builder);
        }
        return builder.build();
    }

    /**
     * @return A title of the column.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title of the column
     * @return The object itself (fluent api)
     */
    @SuppressWarnings("unchecked")
    public A withTitle(String title) {
        this.title = title;
        return (A) this;
    }

    /**
     * Decorator is a post creation callback to alter the behaviour of the
     * column definition.
     *
     * @param decorator
     *            The decorator.
     * @return The object itself (fluent api)
     */
    @SuppressWarnings("unchecked")
    public A withDecorator(Consumer<ColumnDefinition.Builder> decorator) {
        this.decorator = decorator;
        return (A) this;
    }

    /**
     * Returns the builder implementation of the corresponding column
     * definition.
     *
     * @return The column definition builder.
     */
    protected abstract ColumnDefinition.Builder getColumnBuilder();

    /**
     * Extracts the data from the context.
     *
     * @param context
     *            The context to process.
     * @return The extracted data.
     */
    public abstract T getData(C context);

    /**
     * @return the default title
     */
    protected abstract String getDefaultTitle();

}
