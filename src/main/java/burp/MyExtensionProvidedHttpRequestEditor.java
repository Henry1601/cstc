package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import de.usd.cstchef.view.RecipePanel;
import de.usd.cstchef.view.View;
import de.usd.cstchef.view.filter.FilterState;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MyExtensionProvidedHttpRequestEditor implements ExtensionProvidedHttpRequestEditor
{
    private final RawEditor requestEditor;
    private HttpRequestResponse requestResponse;
    private final MontoyaApi api;
    private final View view;
    private final ToolType toolType;

    MyExtensionProvidedHttpRequestEditor(EditorCreationContext creationContext, View view)
    {
        this.api = BurpUtils.getInstance().getApi();
        this.view = view;
        this.toolType = creationContext.toolSource().toolType();
        requestEditor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY);
        requestEditor.uiComponent().addFocusListener(new CstcFocusRequestListener(this));
    }

    @Override
    public HttpRequest getRequest()
    {
        return requestResponse.request();
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse)
    {
        this.requestResponse = requestResponse;

        if (requestResponse == null || requestResponse.request() == null) {
            this.requestEditor.setContents(ByteArray.byteArray());
            return;
        }

        ByteArray originalRequest = requestResponse.request().toByteArray();
        ByteArray result = originalRequest;

        for (RecipePanel recipePanel : getOrderedRecipePanels(FilterState.BurpOperation.OUTGOING, toolType)) {
            result = recipePanel.bake(result, originalRequest);
        }

        this.requestEditor.setContents(result);
    }

    public void reapplyRecipe()
    {
        if (this.requestResponse == null) {
            return;
        }

        this.setRequestResponse(this.requestResponse);
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse)
    {
        return requestResponse.request() != null;
    }

    @Override
    public String caption()
    {
        return "CSTC";
    }

    @Override
    public Component uiComponent()
    {
        return requestEditor.uiComponent();
    }

    @Override
    public Selection selectedData()
    {
        return requestEditor.selection().isPresent() ? requestEditor.selection().get() : null;
    }

    @Override
    public boolean isModified()
    {
        return requestEditor.isModified();
    }

    private List<RecipePanel> getOrderedRecipePanels(FilterState.BurpOperation operation, ToolType toolType)
    {
        List<OrderedRecipePanel> orderedRecipePanels = new ArrayList<>();
        int recipeIndex = 0;

        for (RecipePanel recipePanel : view.getFilterableRecipePanels()) {
            if (!recipePanel.getOperation().equals(operation)) {
                recipeIndex++;
                continue;
            }

            int selectionOrder = BurpUtils.getInstance().getFilterState()
                    .getFilterSelectionOrder(operation, recipePanel.getRecipeName(), toolType);
            if (selectionOrder <= 0) {
                recipeIndex++;
                continue;
            }

            orderedRecipePanels.add(new OrderedRecipePanel(recipePanel, selectionOrder, recipeIndex));
            recipeIndex++;
        }

        orderedRecipePanels.sort(Comparator
                .comparingInt(OrderedRecipePanel::getSelectionOrder)
                .thenComparingInt(OrderedRecipePanel::getRecipeIndex));

        List<RecipePanel> sortedRecipePanels = new ArrayList<>();
        for (OrderedRecipePanel orderedRecipePanel : orderedRecipePanels) {
            sortedRecipePanels.add(orderedRecipePanel.getRecipePanel());
        }

        return sortedRecipePanels;
    }

    private static class OrderedRecipePanel
    {
        private final RecipePanel recipePanel;
        private final int selectionOrder;
        private final int recipeIndex;

        private OrderedRecipePanel(RecipePanel recipePanel, int selectionOrder, int recipeIndex)
        {
            this.recipePanel = recipePanel;
            this.selectionOrder = selectionOrder;
            this.recipeIndex = recipeIndex;
        }

        private RecipePanel getRecipePanel()
        {
            return recipePanel;
        }

        private int getSelectionOrder()
        {
            return selectionOrder;
        }

        private int getRecipeIndex()
        {
            return recipeIndex;
        }
    }

    private static class CstcFocusRequestListener implements FocusListener
    {
        private final MyExtensionProvidedHttpRequestEditor requestEditor;

        private CstcFocusRequestListener(MyExtensionProvidedHttpRequestEditor requestEditor)
        {
            this.requestEditor = requestEditor;
        }

        @Override
        public void focusGained(FocusEvent e)
        {
            this.requestEditor.reapplyRecipe();
        }

        @Override
        public void focusLost(FocusEvent e)
        {
            // Not needed
        }
    }
}