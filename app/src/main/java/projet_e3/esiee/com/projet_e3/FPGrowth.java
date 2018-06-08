//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package projet_e3.esiee.com.projet_e3;

import android.util.Log;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import weka.associations.AbstractAssociator;
import weka.associations.AssociationRule;
import weka.associations.AssociationRules;
import weka.associations.AssociationRulesProducer;
import weka.associations.BinaryItem;
import weka.associations.DefaultAssociationRule;
import weka.associations.DefaultAssociationRule.METRIC_TYPE;
import weka.associations.Item;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.converters.ArffLoader;

public class FPGrowth extends AbstractAssociator implements AssociationRulesProducer, OptionHandler, TechnicalInformationHandler {
    private static final long serialVersionUID = 3620717108603442911L;
    protected int m_numRulesToFind = 10;
    protected double m_upperBoundMinSupport = 1.0D;
    protected double m_lowerBoundMinSupport = 0.1D;
    protected double m_delta = 0.05D;
    protected int m_numInstances;
    protected int m_offDiskReportingFrequency = 10000;
    protected boolean m_findAllRulesForSupportLevel = false;
    protected int m_positiveIndex = 2;
    protected METRIC_TYPE m_metric;
    protected double m_metricThreshold;
    protected FPGrowth.FrequentItemSets m_largeItemSets;
    protected List<AssociationRule> m_rules;
    protected int m_maxItems;
    protected String m_transactionsMustContain;
    protected boolean m_mustContainOR;
    protected String m_rulesMustContain;

    private static void nextSubset(boolean[] subset) {
        for(int i = 0; i < subset.length; ++i) {
            if (!subset[i]) {
                subset[i] = true;
                break;
            }

            subset[i] = false;
        }

    }

    private static Collection<Item> getPremise(FPGrowth.FrequentBinaryItemSet fis, boolean[] subset) {
        boolean ok = false;

        for(int i = 0; i < subset.length; ++i) {
            if (!subset[i]) {
                ok = true;
                break;
            }
        }

        if (!ok) {
            return null;
        } else {
            List<Item> premise = new ArrayList();
            ArrayList<Item> items = new ArrayList(fis.getItems());

            for(int i = 0; i < subset.length; ++i) {
                if (subset[i]) {
                    premise.add(items.get(i));
                }
            }

            return premise;
        }
    }

    private static Collection<Item> getConsequence(FPGrowth.FrequentBinaryItemSet fis, boolean[] subset) {
        List<Item> consequence = new ArrayList();
        ArrayList<Item> items = new ArrayList(fis.getItems());

        for(int i = 0; i < subset.length; ++i) {
            if (!subset[i]) {
                consequence.add(items.get(i));
            }
        }

        return consequence;
    }

    public static List<AssociationRule> generateRulesBruteForce(FPGrowth.FrequentItemSets largeItemSets, METRIC_TYPE metricToUse, double metricThreshold, int upperBoundMinSuppAsInstances, int lowerBoundMinSuppAsInstances, int totalTransactions) {
        List<AssociationRule> rules = new ArrayList();
        largeItemSets.sort();
        Map<Collection<BinaryItem>, Integer> frequencyLookup = new HashMap();
        Iterator setI = largeItemSets.iterator();

        while(true) {
            FPGrowth.FrequentBinaryItemSet fis;
            do {
                if (!setI.hasNext()) {
                    return rules;
                }

                fis = (FPGrowth.FrequentBinaryItemSet)setI.next();
                frequencyLookup.put(fis.getItems(), fis.getSupport());
            } while(fis.getItems().size() <= 1);

            boolean[] subset = new boolean[fis.getItems().size()];
            Collection<Item> premise = null;

            for(Collection consequence = null; (premise = getPremise(fis, subset)) != null; nextSubset(subset)) {
                if (premise.size() > 0 && premise.size() < fis.getItems().size()) {
                    consequence = getConsequence(fis, subset);
                    int totalSupport = fis.getSupport();
                    int supportPremise = (Integer)frequencyLookup.get(premise);
                    int supportConsequence = (Integer)frequencyLookup.get(consequence);
                    DefaultAssociationRule candidate = new DefaultAssociationRule(premise, consequence, metricToUse, supportPremise, supportConsequence, totalSupport, totalTransactions);
                    if (candidate.getPrimaryMetricValue() > metricThreshold && candidate.getTotalSupport() >= lowerBoundMinSuppAsInstances && candidate.getTotalSupport() <= upperBoundMinSuppAsInstances) {
                        rules.add(candidate);
                    }
                }
            }
        }
    }

    public static List<AssociationRule> pruneRules(List<AssociationRule> rulesToPrune, ArrayList<Item> itemsToConsider, boolean useOr) {
        ArrayList<AssociationRule> result = new ArrayList();
        Iterator var4 = rulesToPrune.iterator();

        while(var4.hasNext()) {
            AssociationRule r = (AssociationRule)var4.next();
            if (r.containsItems(itemsToConsider, useOr)) {
                result.add(r);
            }
        }

        return result;
    }

    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();
        result.enable(Capability.UNARY_ATTRIBUTES);
        result.enable(Capability.BINARY_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);
        result.enable(Capability.NO_CLASS);
        return result;
    }

    public String globalInfo() {
        return "Class implementing the FP-growth algorithm for finding large item sets without candidate generation. Iteratively reduces the minimum support until it finds the required number of rules with the given minimum metric. For more information see:\n\n" + this.getTechnicalInformation().toString();
    }

    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation result = new TechnicalInformation(Type.INPROCEEDINGS);
        result.setValue(Field.AUTHOR, "J. Han and J.Pei and Y. Yin");
        result.setValue(Field.TITLE, "Mining frequent patterns without candidate generation");
        result.setValue(Field.BOOKTITLE, "Proceedings of the 2000 ACM-SIGMID International Conference on Management of Data");
        result.setValue(Field.YEAR, "2000");
        result.setValue(Field.PAGES, "1-12");
        return result;
    }

    private boolean passesMustContain(Instance inst, boolean[] transactionsMustContainIndexes, int numInTransactionsMustContainList) {
        boolean result = false;
        int containsCount;
        int i;
        if (inst instanceof SparseInstance) {
            containsCount = 0;

            for(i = 0; i < inst.numValues(); ++i) {
                int attIndex = inst.index(i);
                if (this.m_mustContainOR) {
                    if (transactionsMustContainIndexes[attIndex]) {
                        return true;
                    }
                } else if (transactionsMustContainIndexes[attIndex]) {
                    ++containsCount;
                }
            }

            if (!this.m_mustContainOR && containsCount == numInTransactionsMustContainList) {
                return true;
            }
        } else {
            containsCount = 0;

            for(i = 0; i < transactionsMustContainIndexes.length; ++i) {
                if (transactionsMustContainIndexes[i] && (int)inst.value(i) == this.m_positiveIndex - 1) {
                    if (this.m_mustContainOR) {
                        return true;
                    }

                    ++containsCount;
                }
            }

            if (!this.m_mustContainOR && containsCount == numInTransactionsMustContainList) {
                return true;
            }
        }

        return result;
    }

    private void processSingleton(Instance current, ArrayList<BinaryItem> singletons) throws Exception {
        int j;
        if (current instanceof SparseInstance) {
            for(j = 0; j < current.numValues(); ++j) {
                int attIndex = current.index(j);
                ((BinaryItem)singletons.get(attIndex)).increaseFrequency();
            }
        } else {
            for(j = 0; j < current.numAttributes(); ++j) {
                if (!current.isMissing(j) && (current.attribute(j).numValues() == 1 || current.value(j) == (double)(this.m_positiveIndex - 1))) {
                    ((BinaryItem)singletons.get(j)).increaseFrequency();
                }
            }
        }

    }

    protected ArrayList<BinaryItem> getSingletons(Object source) throws Exception {
        ArrayList<BinaryItem> singletons = new ArrayList();
        Instances data = null;
        if (source instanceof Instances) {
            data = (Instances)source;
        } else if (source instanceof ArffLoader) {
            data = ((ArffLoader)source).getStructure();
        }

        int i;
        for(i = 0; i < data.numAttributes(); ++i) {
            singletons.add(new BinaryItem(data.attribute(i), this.m_positiveIndex - 1));
        }

        Instance current;
        if (source instanceof Instances) {
            this.m_numInstances = data.numInstances();

            for(i = 0; i < data.numInstances(); ++i) {
                current = data.instance(i);
                this.processSingleton(current, singletons);
            }
        } else if (source instanceof ArffLoader) {
            ArffLoader loader = (ArffLoader)source;
            current = null;
            int count = 0;

            while((current = loader.getNextInstance(data)) != null) {
                this.processSingleton(current, singletons);
                ++count;
                if (count % this.m_offDiskReportingFrequency == 0) {
                    System.err.println("Singletons: done " + count);
                }
            }

            this.m_numInstances = count;
            loader.reset();
        }

        return singletons;
    }

    protected ArrayList<BinaryItem> getSingletons(Instances data) throws Exception {
        return this.getSingletons((Object)data);
    }

    private void insertInstance(Instance current, ArrayList<BinaryItem> singletons, FPGrowth.FPTreeRoot tree, int minSupport) {
        ArrayList<BinaryItem> transaction = new ArrayList();
        int j;
        if (current instanceof SparseInstance) {
            for(j = 0; j < current.numValues(); ++j) {
                int attIndex = current.index(j);
                if (((BinaryItem)singletons.get(attIndex)).getFrequency() >= minSupport) {
                    //Log.i("currentValue", "" + ((BinaryItem)singletons.get(attIndex)).getFrequency());
                    transaction.add(singletons.get(attIndex));
                }
            }

            Collections.sort(transaction);
            tree.addItemSet(transaction, 1);
        } else {
            for(j = 0; j < current.numAttributes(); ++j) {
                if (!current.isMissing(j) && (current.attribute(j).numValues() == 1 || current.value(j) == (double)(this.m_positiveIndex - 1)) && ((BinaryItem)singletons.get(j)).getFrequency() >= minSupport) {
                    transaction.add(singletons.get(j));
                }
            }

            Collections.sort(transaction);
            tree.addItemSet(transaction, 1);
        }
    }

    /*protected FPTreeRoot buildFPTree(ArrayList<BinaryItem> singletons,Instances data, int minSupport){
        double minWeight = 0.8;
        FPTreeRoot tree = new FPTreeRoot();
        for(int a=0; a<data.numAttributes();a++){
            //data.attribute(a).setWeight(Math.random());
            for (int i = 0; i < data.numInstances(); i++) {
                Instance current = data.instance(i);
                ArrayList<BinaryItem> transaction = new ArrayList<BinaryItem>();
                if (current instanceof SparseInstance){
                    for (int j = 0; j < current.numValues(); j++) {
                        int attIndex = current.index(j);
                        if (singletons.get(attIndex).getFrequency() >= minSupport && data.attribute(a).weight()>=minWeight) {
                            transaction.add(singletons.get(attIndex));
                        }
                    }
                    Collections.sort(transaction);
                    tree.addItemSet(transaction, 1);
                } else {
                    for (int j = 0; j < data.numAttributes(); j++) {
                        if (!current.isMissing(j)) {
                            if (current.attribute(j).numValues() == 1 || current.value(j) == m_positiveIndex - 1) {
                                if (singletons.get(j).getFrequency() >= minSupport && data.attribute(a).weight()>=minWeight) {
                                    transaction.add(singletons.get(j));
                                }
                            }
                        }
                    }
                    Collections.sort(transaction);
                    tree.addItemSet(transaction, 1);
                }
            }
        }
        return tree;
    }*/

    protected FPGrowth.FPTreeRoot buildFPTree(ArrayList<BinaryItem> singletons, Object dataSource, int minSupport) throws Exception {
        FPGrowth.FPTreeRoot tree = new FPGrowth.FPTreeRoot();
        Instances data = null;
        if (dataSource instanceof Instances) {
            data = (Instances)dataSource;
        } else if (dataSource instanceof ArffLoader) {
            data = ((ArffLoader)dataSource).getStructure();
        }

        if (dataSource instanceof Instances) {
            for(int i = 0; i < data.numInstances(); ++i) {
                this.insertInstance(data.instance(i), singletons, tree, minSupport);
            }
        } else if (dataSource instanceof ArffLoader) {
            ArffLoader loader = (ArffLoader)dataSource;
            Instance current = null;
            int count = 0;

            while((current = loader.getNextInstance(data)) != null) {
                this.insertInstance(current, singletons, tree, minSupport);
                ++count;
                if (count % this.m_offDiskReportingFrequency == 0) {
                    System.err.println("build tree done: " + count);
                }
            }
        }

        return tree;
    }

    protected void mineTree(FPGrowth.FPTreeRoot tree, FPGrowth.FrequentItemSets largeItemSets, int recursionLevel, FPGrowth.FrequentBinaryItemSet conditionalItems, int minSupport) {
        if (!tree.isEmpty(recursionLevel)) {
            if (this.m_maxItems <= 0 || recursionLevel < this.m_maxItems) {
                Map<BinaryItem, FPGrowth.FPTreeRoot.Header> headerTable = tree.getHeaderTable();
                Set<BinaryItem> keys = headerTable.keySet();
                Iterator i = keys.iterator();

                label70:
                while(true) {
                    BinaryItem item;
                    FPGrowth.FPTreeRoot.Header itemHeader;
                    int support;
                    do {
                        if (!i.hasNext()) {
                            return;
                        }

                        item = (BinaryItem)i.next();
                        itemHeader = (FPGrowth.FPTreeRoot.Header)headerTable.get(item);
                        support = itemHeader.getProjectedCounts().getCount(recursionLevel);
                    } while(support < minSupport);

                    Iterator var12 = itemHeader.getHeaderList().iterator();

                    while(true) {
                        FPGrowth.FPTreeNode n;
                        int currentCount;
                        FPGrowth.FPTreeNode temp;
                        do {
                            if (!var12.hasNext()) {
                                FPGrowth.FrequentBinaryItemSet newConditional = (FPGrowth.FrequentBinaryItemSet)conditionalItems.clone();
                                newConditional.addItem(item);
                                newConditional.setSupport(support);
                                largeItemSets.addItemSet(newConditional);
                                this.mineTree(tree, largeItemSets, recursionLevel + 1, newConditional, minSupport);
                                Iterator var17 = itemHeader.getHeaderList().iterator();

                                while(var17.hasNext()) {
                                    n = (FPGrowth.FPTreeNode)var17.next();

                                    for(temp = n.getParent(); temp != tree; temp = temp.getParent()) {
                                        temp.removeProjectedCount(recursionLevel + 1);
                                    }
                                }

                                var17 = headerTable.values().iterator();

                                while(var17.hasNext()) {
                                    FPGrowth.FPTreeRoot.Header h = (FPGrowth.FPTreeRoot.Header)var17.next();
                                    h.getProjectedCounts().removeCount(recursionLevel + 1);
                                }
                                continue label70;
                            }

                            n = (FPGrowth.FPTreeNode)var12.next();
                            currentCount = n.getProjectedCount(recursionLevel);
                        } while(currentCount <= 0);

                        for(temp = n.getParent(); temp != tree; temp = temp.getParent()) {
                            temp.increaseProjectedCount(recursionLevel + 1, currentCount);
                            ((FPGrowth.FPTreeRoot.Header)headerTable.get(temp.getItem())).getProjectedCounts().increaseCount(recursionLevel + 1, currentCount);
                        }
                    }
                }
            }
        }
    }

    public FPGrowth() {
        this.m_metric = METRIC_TYPE.CONFIDENCE;
        this.m_metricThreshold = 0.9D;
        this.m_maxItems = -1;
        this.m_transactionsMustContain = "";
        this.m_mustContainOR = false;
        this.m_rulesMustContain = "";
        this.resetOptions();
    }

    public void resetOptions() {
        this.m_delta = 0.05D;
        this.m_metricThreshold = 0.9D;
        this.m_numRulesToFind = 10;
        this.m_lowerBoundMinSupport = 0.1D;
        this.m_upperBoundMinSupport = 1.0D;
        this.m_positiveIndex = 2;
        this.m_transactionsMustContain = "";
        this.m_rulesMustContain = "";
        this.m_mustContainOR = false;
    }

    public String positiveIndexTipText() {
        return "Set the index of binary valued attributes that is to be considered the positive index. Has no effect for sparse data (in this case the first index (i.e. non-zero values) is always treated as  positive. Also has no effect for unary valued attributes (i.e. when using the Weka Apriori-style format for market basket data, which uses missing value \"?\" to indicate absence of an item.";
    }

    public void setPositiveIndex(int index) {
        this.m_positiveIndex = index;
    }

    public int getPositiveIndex() {
        return this.m_positiveIndex;
    }

    public void setNumRulesToFind(int numR) {
        this.m_numRulesToFind = numR;
    }

    public int getNumRulesToFind() {
        return this.m_numRulesToFind;
    }

    public String numRulesToFindTipText() {
        return "The number of rules to output";
    }

    public void setMetricType(SelectedTag d) {
        int ordinal = d.getSelectedTag().getID();
        METRIC_TYPE[] var3 = METRIC_TYPE.values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            METRIC_TYPE m = var3[var5];
            if (m.ordinal() == ordinal) {
                this.m_metric = m;
                break;
            }
        }

    }

    public void setMaxNumberOfItems(int max) {
        this.m_maxItems = max;
    }

    public int getMaxNumberOfItems() {
        return this.m_maxItems;
    }

    public String maxNumberOfItemsTipText() {
        return "The maximum number of items to include in frequent item sets. -1 means no limit.";
    }

    public SelectedTag getMetricType() {
        return new SelectedTag(this.m_metric.ordinal(), DefaultAssociationRule.TAGS_SELECTION);
    }

    public String metricTypeTipText() {
        return "Set the type of metric by which to rank rules. Confidence is the proportion of the examples covered by the premise that are also covered by the consequence(Class association rules can only be mined using confidence). Lift is confidence divided by the proportion of all examples that are covered by the consequence. This is a measure of the importance of the association that is independent of support. Leverage is the proportion of additional examples covered by both the premise and consequence above those expected if the premise and consequence were independent of each other. The total number of examples that this represents is presented in brackets following the leverage. Conviction is another measure of departure from independence.";
    }

    public String minMetricTipText() {
        return "Minimum metric score. Consider only rules with scores higher than this value.";
    }

    public double getMinMetric() {
        return this.m_metricThreshold;
    }

    public void setMinMetric(double v) {
        this.m_metricThreshold = v;
    }

    public String transactionsMustContainTipText() {
        return "Limit input to FPGrowth to those transactions (instances) that contain these items. Provide a comma separated list of attribute names.";
    }

    public void setTransactionsMustContain(String list) {
        this.m_transactionsMustContain = list;
    }

    public String getTransactionsMustContain() {
        return this.m_transactionsMustContain;
    }

    public String rulesMustContainTipText() {
        return "Only print rules that contain these items. Provide a comma separated list of attribute names.";
    }

    public void setRulesMustContain(String list) {
        this.m_rulesMustContain = list;
    }

    public String getRulesMustContain() {
        return this.m_rulesMustContain;
    }

    public String useORForMustContainListTipText() {
        return "Use OR instead of AND for transactions/rules must contain lists.";
    }

    public void setUseORForMustContainList(boolean b) {
        this.m_mustContainOR = b;
    }

    public boolean getUseORForMustContainList() {
        return this.m_mustContainOR;
    }

    public String deltaTipText() {
        return "Iteratively decrease support by this factor. Reduces support until min support is reached or required number of rules has been generated.";
    }

    public double getDelta() {
        return this.m_delta;
    }

    public void setDelta(double v) {
        this.m_delta = v;
    }

    public String lowerBoundMinSupportTipText() {
        return "Lower bound for minimum support as a fraction or number of instances.";
    }

    public double getLowerBoundMinSupport() {
        return this.m_lowerBoundMinSupport;
    }

    public void setLowerBoundMinSupport(double v) {
        this.m_lowerBoundMinSupport = v;
    }

    public String upperBoundMinSupportTipText() {
        return "Upper bound for minimum support as a fraction or number of instances. Start iteratively decreasing minimum support from this value.";
    }

    public double getUpperBoundMinSupport() {
        return this.m_upperBoundMinSupport;
    }

    public void setUpperBoundMinSupport(double v) {
        this.m_upperBoundMinSupport = v;
    }

    public String findAllRulesForSupportLevelTipText() {
        return "Find all rules that meet the lower bound on minimum support and the minimum metric constraint. Turning this mode on will disable the iterative support reduction procedure to find the specified number of rules.";
    }

    public void setFindAllRulesForSupportLevel(boolean s) {
        this.m_findAllRulesForSupportLevel = s;
    }

    public boolean getFindAllRulesForSupportLevel() {
        return this.m_findAllRulesForSupportLevel;
    }

    public void setOffDiskReportingFrequency(int freq) {
        this.m_offDiskReportingFrequency = freq;
    }

    public AssociationRules getAssociationRules() {
        List<AssociationRule> rulesToReturn = new ArrayList();
        int count = 0;
        Iterator var3 = this.m_rules.iterator();

        while(var3.hasNext()) {
            AssociationRule r = (AssociationRule)var3.next();
            rulesToReturn.add(r);
            ++count;
            if (!this.m_findAllRulesForSupportLevel && count == this.m_numRulesToFind) {
                break;
            }
        }

        return new AssociationRules(rulesToReturn, this);
    }

    public String[] getRuleMetricNames() {
        String[] metricNames = new String[DefaultAssociationRule.TAGS_SELECTION.length];

        for(int i = 0; i < DefaultAssociationRule.TAGS_SELECTION.length; ++i) {
            metricNames[i] = DefaultAssociationRule.TAGS_SELECTION[i].getReadable();
        }

        return metricNames;
    }

    public boolean canProduceRules() {
        return true;
    }

    public Enumeration<Option> listOptions() {
        Vector<Option> newVector = new Vector();
        String string00 = "\tSet the index of the attribute value to consider as 'positive'\n\tfor binary attributes in normal dense instances. Index 2 is always\n\tused for sparse instances. (default = 2)";
        String string0 = "\tThe maximum number of items to include in large items sets (and rules). (default = -1, i.e. no limit.)";
        String string1 = "\tThe required number of rules. (default = " + this.m_numRulesToFind + ")";
        String string2 = "\tThe minimum metric score of a rule. (default = " + this.m_metricThreshold + ")";
        String string3 = "\tThe metric by which to rank rules. (default = confidence)";
        String string4 = "\tThe lower bound for the minimum support as a fraction or number of instances. (default = " + this.m_lowerBoundMinSupport + ")";
        String string5 = "\tUpper bound for minimum support as a fraction or number of instances. (default = 1.0)";
        String string6 = "\tThe delta by which the minimum support is decreased in\n\teach iteration as a fraction or number of instances. (default = " + this.m_delta + ")";
        String string7 = "\tFind all rules that meet the lower bound on\n\tminimum support and the minimum metric constraint.\n\tTurning this mode on will disable the iterative support reduction\n\tprocedure to find the specified number of rules.";
        String string8 = "\tOnly consider transactions that contain these items (default = no restriction)";
        String string9 = "\tOnly print rules that contain these items. (default = no restriction)";
        String string10 = "\tUse OR instead of AND for must contain list(s). Use in conjunction\n\twith -transactions and/or -rules";
        newVector.add(new Option(string00, "P", 1, "-P <attribute index of positive value>"));
        newVector.add(new Option(string0, "I", 1, "-I <max items>"));
        newVector.add(new Option(string1, "N", 1, "-N <require number of rules>"));
        newVector.add(new Option(string3, "T", 1, "-T <0=confidence | 1=lift | 2=leverage | 3=Conviction>"));
        newVector.add(new Option(string2, "C", 1, "-C <minimum metric score of a rule>"));
        newVector.add(new Option(string5, "U", 1, "-U <upper bound for minimum support>"));
        newVector.add(new Option(string4, "M", 1, "-M <lower bound for minimum support>"));
        newVector.add(new Option(string6, "D", 1, "-D <delta for minimum support>"));
        newVector.add(new Option(string7, "S", 0, "-S"));
        newVector.add(new Option(string8, "transactions", 1, "-transactions <comma separated list of attribute names>"));
        newVector.add(new Option(string9, "rules", 1, "-rules <comma separated list of attribute names>"));
        newVector.add(new Option(string10, "use-or", 0, "-use-or"));
        return newVector.elements();
    }

    public void setOptions(String[] options) throws Exception {
        this.resetOptions();
        String positiveIndexString = Utils.getOption('P', options);
        String maxItemsString = Utils.getOption('I', options);
        String numRulesString = Utils.getOption('N', options);
        String minMetricString = Utils.getOption('C', options);
        String metricTypeString = Utils.getOption("T", options);
        String lowerBoundSupportString = Utils.getOption("M", options);
        String upperBoundSupportString = Utils.getOption("U", options);
        String deltaString = Utils.getOption("D", options);
        String transactionsString = Utils.getOption("transactions", options);
        String rulesString = Utils.getOption("rules", options);
        if (positiveIndexString.length() != 0) {
            this.setPositiveIndex(Integer.parseInt(positiveIndexString));
        }

        if (maxItemsString.length() != 0) {
            this.setMaxNumberOfItems(Integer.parseInt(maxItemsString));
        }

        if (metricTypeString.length() != 0) {
            this.setMetricType(new SelectedTag(Integer.parseInt(metricTypeString), DefaultAssociationRule.TAGS_SELECTION));
        }

        if (numRulesString.length() != 0) {
            this.setNumRulesToFind(Integer.parseInt(numRulesString));
        }

        if (minMetricString.length() != 0) {
            this.setMinMetric(Double.parseDouble(minMetricString));
        }

        if (deltaString.length() != 0) {
            this.setDelta(Double.parseDouble(deltaString));
        }

        if (lowerBoundSupportString.length() != 0) {
            this.setLowerBoundMinSupport(Double.parseDouble(lowerBoundSupportString));
        }

        if (upperBoundSupportString.length() != 0) {
            this.setUpperBoundMinSupport(Double.parseDouble(upperBoundSupportString));
        }

        if (transactionsString.length() != 0) {
            this.setTransactionsMustContain(transactionsString);
        }

        if (rulesString.length() > 0) {
            this.setRulesMustContain(rulesString);
        }

        this.setUseORForMustContainList(Utils.getFlag("use-or", options));
        this.setFindAllRulesForSupportLevel(Utils.getFlag('S', options));
    }

    public String[] getOptions() {
        ArrayList<String> options = new ArrayList();
        options.add("-P");
        options.add("" + this.getPositiveIndex());
        options.add("-I");
        options.add("" + this.getMaxNumberOfItems());
        options.add("-N");
        options.add("" + this.getNumRulesToFind());
        options.add("-T");
        options.add("" + this.getMetricType().getSelectedTag().getID());
        options.add("-C");
        options.add("" + this.getMinMetric());
        options.add("-D");
        options.add("" + this.getDelta());
        options.add("-U");
        options.add("" + this.getUpperBoundMinSupport());
        options.add("-M");
        options.add("" + this.getLowerBoundMinSupport());
        if (this.getFindAllRulesForSupportLevel()) {
            options.add("-S");
        }

        if (this.getTransactionsMustContain().length() > 0) {
            options.add("-transactions");
            options.add(this.getTransactionsMustContain());
        }

        if (this.getRulesMustContain().length() > 0) {
            options.add("-rules");
            options.add(this.getRulesMustContain());
        }

        if (this.getUseORForMustContainList()) {
            options.add("-use-or");
        }

        return (String[])options.toArray(new String[1]);
    }

    private Instances parseTransactionsMustContain(Instances data) {
        String[] split = this.m_transactionsMustContain.trim().split(",");
        boolean[] transactionsMustContainIndexes = new boolean[data.numAttributes()];
        int numInTransactionsMustContainList = split.length;

        for(int i = 0; i < split.length; ++i) {
            String attName = split[i].trim();
            Attribute att = data.attribute(attName);
            if (att == null) {
                System.err.println("[FPGrowth] : WARNING - can't find attribute " + attName + " in the data.");
                --numInTransactionsMustContainList;
            } else {
                transactionsMustContainIndexes[att.index()] = true;
            }
        }

        if (numInTransactionsMustContainList == 0) {
            return data;
        } else {
            Instances newInsts = new Instances(data, 0);

            for(int i = 0; i < data.numInstances(); ++i) {
                if (this.passesMustContain(data.instance(i), transactionsMustContainIndexes, numInTransactionsMustContainList)) {
                    newInsts.add(data.instance(i));
                }
            }

            newInsts.compactify();
            return newInsts;
        }
    }

    private ArrayList<Item> parseRulesMustContain(Instances data) {
        ArrayList<Item> result = new ArrayList();
        String[] split = this.m_rulesMustContain.trim().split(",");

        for(int i = 0; i < split.length; ++i) {
            String attName = split[i].trim();
            Attribute att = data.attribute(attName);
            if (att == null) {
                System.err.println("[FPGrowth] : WARNING - can't find attribute " + attName + " in the data.");
            } else {
                BinaryItem tempI = null;

                try {
                    tempI = new BinaryItem(att, this.m_positiveIndex - 1);
                } catch (Exception var9) {
                    var9.printStackTrace();
                }

                result.add(tempI);
            }
        }

        return result;
    }

    public void getFrequentItems(Instances data) throws Exception {
        Object source = (Object)data;
        Capabilities capabilities = this.getCapabilities();
        boolean arffLoader = false;
        boolean breakOnNext = false;

        capabilities.testWithFail(data);
        if (this.m_transactionsMustContain.length() > 0 && source instanceof Instances) {
            data = this.parseTransactionsMustContain(data);
            this.getCapabilities().testWithFail(data);
        }

        ArrayList<Item> rulesMustContain = null;
        if (this.m_rulesMustContain.length() > 0) {
            rulesMustContain = this.parseRulesMustContain(data);
        }

        ArrayList<BinaryItem> singletons = this.getSingletons(source);
        int upperBoundMinSuppAsInstances = this.m_upperBoundMinSupport > 1.0D ? (int)this.m_upperBoundMinSupport : (int)Math.ceil(this.m_upperBoundMinSupport * (double)this.m_numInstances);
        int lowerBoundMinSuppAsInstances = this.m_lowerBoundMinSupport > 1.0D ? (int)this.m_lowerBoundMinSupport : (int)Math.ceil(this.m_lowerBoundMinSupport * (double)this.m_numInstances);
        double var10000;
        if (this.m_upperBoundMinSupport > 1.0D) {
            var10000 = this.m_upperBoundMinSupport / (double)this.m_numInstances;
        } else {
            var10000 = this.m_upperBoundMinSupport;
        }

        double lowerBoundMinSuppAsFraction = this.m_lowerBoundMinSupport > 1.0D ? this.m_lowerBoundMinSupport / (double)this.m_numInstances : this.m_lowerBoundMinSupport;
        double deltaAsFraction = this.m_delta > 1.0D ? this.m_delta / (double)this.m_numInstances : this.m_delta;
        double currentSupport = 1.0D;
        if (this.m_findAllRulesForSupportLevel) {
            currentSupport = lowerBoundMinSuppAsFraction;
        }

        do {
            int currentSupportAsInstances = currentSupport > 1.0D ? (int)currentSupport : (int)Math.ceil(currentSupport * (double)this.m_numInstances);

            FPGrowth.FPTreeRoot tree = this.buildFPTree(singletons,(Instances) source, currentSupportAsInstances);
            FPGrowth.FrequentItemSets largeItemSets = new FPGrowth.FrequentItemSets(this.m_numInstances);

            FPGrowth.FrequentBinaryItemSet conditionalItems = new FPGrowth.FrequentBinaryItemSet(new ArrayList(), 0);
            this.mineTree(tree, largeItemSets, 0, conditionalItems, currentSupportAsInstances);
            this.m_largeItemSets = largeItemSets;

            this.m_rules = generateRulesBruteForce(this.m_largeItemSets, this.m_metric, this.m_metricThreshold, upperBoundMinSuppAsInstances, lowerBoundMinSuppAsInstances, this.m_numInstances);

            if (this.m_findAllRulesForSupportLevel || breakOnNext) {
                break;
            }

            currentSupport -= deltaAsFraction;
            if (currentSupport < lowerBoundMinSuppAsFraction) {
                if (currentSupport + deltaAsFraction <= lowerBoundMinSuppAsFraction) {
                    break;
                }

                currentSupport = lowerBoundMinSuppAsFraction;
                breakOnNext = true;
            }
        } while(this.m_rules.size() < this.m_numRulesToFind);

        Collections.sort(this.m_rules);
    }

    private void buildAssociations(Object source) throws Exception {
        Instances data = null;
        Capabilities capabilities = this.getCapabilities();
        boolean arffLoader = false;
        boolean breakOnNext = false;
        if (source instanceof ArffLoader) {
            data = ((ArffLoader)source).getStructure();
            capabilities.setMinimumNumberInstances(0);
            arffLoader = true;
        } else {
            data = (Instances)source;
        }

        capabilities.testWithFail(data);
        if (this.m_transactionsMustContain.length() > 0 && source instanceof Instances) {
            data = this.parseTransactionsMustContain(data);
            this.getCapabilities().testWithFail(data);
        }

        ArrayList<Item> rulesMustContain = null;
        if (this.m_rulesMustContain.length() > 0) {
            rulesMustContain = this.parseRulesMustContain(data);
        }

        ArrayList<BinaryItem> singletons = this.getSingletons(source);
        int upperBoundMinSuppAsInstances = this.m_upperBoundMinSupport > 1.0D ? (int)this.m_upperBoundMinSupport : (int)Math.ceil(this.m_upperBoundMinSupport * (double)this.m_numInstances);
        int lowerBoundMinSuppAsInstances = this.m_lowerBoundMinSupport > 1.0D ? (int)this.m_lowerBoundMinSupport : (int)Math.ceil(this.m_lowerBoundMinSupport * (double)this.m_numInstances);
        double var10000;
        if (this.m_upperBoundMinSupport > 1.0D) {
            var10000 = this.m_upperBoundMinSupport / (double)this.m_numInstances;
        } else {
            var10000 = this.m_upperBoundMinSupport;
        }

        double lowerBoundMinSuppAsFraction = this.m_lowerBoundMinSupport > 1.0D ? this.m_lowerBoundMinSupport / (double)this.m_numInstances : this.m_lowerBoundMinSupport;
        double deltaAsFraction = this.m_delta > 1.0D ? this.m_delta / (double)this.m_numInstances : this.m_delta;
        double currentSupport = 1.0D;
        if (this.m_findAllRulesForSupportLevel) {
            currentSupport = lowerBoundMinSuppAsFraction;
        }

        do {
            if (arffLoader) {
                ((ArffLoader)source).reset();
            }

            int currentSupportAsInstances = currentSupport > 1.0D ? (int)currentSupport : (int)Math.ceil(currentSupport * (double)this.m_numInstances);
            if (arffLoader) {
                System.err.println("Building FP-tree...");
            }

            FPGrowth.FPTreeRoot tree = this.buildFPTree(singletons,(Instances) source, currentSupportAsInstances);
            FPGrowth.FrequentItemSets largeItemSets = new FPGrowth.FrequentItemSets(this.m_numInstances);
            if (arffLoader) {
                System.err.println("Mining tree for min supp " + currentSupport);
            }

            FPGrowth.FrequentBinaryItemSet conditionalItems = new FPGrowth.FrequentBinaryItemSet(new ArrayList(), 0);
            this.mineTree(tree, largeItemSets, 0, conditionalItems, currentSupportAsInstances);
            this.m_largeItemSets = largeItemSets;
            if (arffLoader) {
                System.err.println("Number of large item sets: " + this.m_largeItemSets.size());
            }

            tree = null;
            this.m_rules = generateRulesBruteForce(this.m_largeItemSets, this.m_metric, this.m_metricThreshold, upperBoundMinSuppAsInstances, lowerBoundMinSuppAsInstances, this.m_numInstances);
            if (arffLoader) {
                System.err.println("Number of rules found " + this.m_rules.size());
            }

            if (rulesMustContain != null && rulesMustContain.size() > 0) {
                this.m_rules = pruneRules(this.m_rules, rulesMustContain, this.m_mustContainOR);
            }

            if (this.m_findAllRulesForSupportLevel || breakOnNext) {
                break;
            }

            currentSupport -= deltaAsFraction;
            if (currentSupport < lowerBoundMinSuppAsFraction) {
                if (currentSupport + deltaAsFraction <= lowerBoundMinSuppAsFraction) {
                    break;
                }

                currentSupport = lowerBoundMinSuppAsFraction;
                breakOnNext = true;
            }
        } while(this.m_rules.size() < this.m_numRulesToFind);

        Collections.sort(this.m_rules);
    }

    public void buildAssociations(Instances data) throws Exception {
        this.buildAssociations((Object)data);
    }

    public String toString() {
        if (this.m_rules == null) {
            return "FPGrowth hasn't been trained yet!";
        } else {
            StringBuffer result = new StringBuffer();
            int numRules = this.m_rules.size() < this.m_numRulesToFind ? this.m_rules.size() : this.m_numRulesToFind;
            if (this.m_rules.size() == 0) {
                return "No rules found!";
            } else {
                result.append("FPGrowth found " + this.m_rules.size() + " rules");
                if (!this.m_findAllRulesForSupportLevel) {
                    result.append(" (displaying top " + numRules + ")");
                }

                if (this.m_transactionsMustContain.length() > 0 || this.m_rulesMustContain.length() > 0) {
                    result.append("\n");
                    if (this.m_transactionsMustContain.length() > 0) {
                        result.append("\nUsing only transactions that contain: " + this.m_transactionsMustContain);
                    }

                    if (this.m_rulesMustContain.length() > 0) {
                        result.append("\nShowing only rules that contain: " + this.m_rulesMustContain);
                    }
                }

                result.append("\n\n");
                int count = 0;
                Iterator var4 = this.m_rules.iterator();

                while(var4.hasNext()) {
                    AssociationRule r = (AssociationRule)var4.next();
                    result.append(Utils.doubleToString((double)count + 1.0D, (int)(Math.log((double)numRules) / Math.log(10.0D) + 1.0D), 0) + ". ");
                    result.append(r + "\n");
                    ++count;
                    if (!this.m_findAllRulesForSupportLevel && count == this.m_numRulesToFind) {
                        break;
                    }
                }

                return result.toString();
            }
        }
    }

    public String graph(FPGrowth.FPTreeRoot tree) {
        StringBuffer text = new StringBuffer();
        text.append("digraph FPTree {\n");
        text.append("N0 [label=\"ROOT\"]\n");
        tree.graphFPTree(text);
        text.append("}\n");
        return text.toString();
    }

    public String getRevision() {
        return RevisionUtils.extract("$Revision: 8034 $");
    }

    public static void main(String[] args) {
        try {
            String[] argsCopy = (String[])args.clone();
            if (Utils.getFlag('h', argsCopy) || Utils.getFlag("help", argsCopy)) {
                runAssociator(new FPGrowth(), args);
                System.out.println("-disk\n\tProcess data off of disk instead of loading\n\tinto main memory. This is a command line only option.");
                return;
            }

            if (!Utils.getFlag("disk", args)) {
                runAssociator(new FPGrowth(), args);
            } else {
                String filename = Utils.getOption('t', args);
                ArffLoader loader = null;
                if (filename.length() == 0) {
                    throw new Exception("No training file specified!");
                }

                loader = new ArffLoader();
                loader.setFile(new File(filename));
                FPGrowth fpGrowth = new FPGrowth();
                fpGrowth.setOptions(args);
                Utils.checkForRemainingOptions(args);
                fpGrowth.buildAssociations((Object)loader);
                System.out.print(fpGrowth.toString());
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    private static class FPTreeRoot extends FPGrowth.FPTreeNode {
        private static final long serialVersionUID = 632150939785333297L;
        protected Map<BinaryItem, FPGrowth.FPTreeRoot.Header> m_headerTable = new HashMap();

        public FPTreeRoot() {
            super((FPGrowth.FPTreeNode)null, (BinaryItem)null);
        }

        public void addItemSet(Collection<BinaryItem> itemSet, int incr) {
            super.addItemSet(itemSet, this.m_headerTable, incr);
        }

        public Map<BinaryItem, FPGrowth.FPTreeRoot.Header> getHeaderTable() {
            return this.m_headerTable;
        }

        public boolean isEmpty(int recursionLevel) {
            Iterator var2 = this.m_children.values().iterator();

            FPGrowth.FPTreeNode c;
            do {
                if (!var2.hasNext()) {
                    return true;
                }

                c = (FPGrowth.FPTreeNode)var2.next();
            } while(c.getProjectedCount(recursionLevel) <= 0);

            return false;
        }

        public String toString(String pad, int recursionLevel) {
            StringBuffer result = new StringBuffer();
            result.append(pad);
            result.append("+ ROOT\n");
            Iterator var4 = this.m_children.values().iterator();

            while(var4.hasNext()) {
                FPGrowth.FPTreeNode node = (FPGrowth.FPTreeNode)var4.next();
                result.append(node.toString(pad + "|  ", recursionLevel));
            }

            return result.toString();
        }

        public String printHeaderTable(int recursionLevel) {
            StringBuffer buffer = new StringBuffer();
            Iterator var3 = this.m_headerTable.keySet().iterator();

            while(var3.hasNext()) {
                BinaryItem item = (BinaryItem)var3.next();
                buffer.append(item.toString());
                buffer.append(" : ");
                buffer.append(((FPGrowth.FPTreeRoot.Header)this.m_headerTable.get(item)).getProjectedCounts().getCount(recursionLevel));
                buffer.append("\n");
            }

            return buffer.toString();
        }

        public void graphHeaderTable(StringBuffer text, int maxID) {
            Iterator var3 = this.m_headerTable.keySet().iterator();

            while(true) {
                FPGrowth.FPTreeRoot.Header h;
                List headerList;
                do {
                    if (!var3.hasNext()) {
                        return;
                    }

                    BinaryItem item = (BinaryItem)var3.next();
                    h = (FPGrowth.FPTreeRoot.Header)this.m_headerTable.get(item);
                    headerList = h.getHeaderList();
                } while(headerList.size() <= 1);

                text.append("N" + maxID + " [label=\"" + ((FPGrowth.FPTreeNode)headerList.get(0)).getItem().toString() + " (" + h.getProjectedCounts().getCount(0) + ")\" shape=plaintext]\n");
                text.append("N" + maxID + "->N" + ((FPGrowth.FPTreeNode)headerList.get(1)).m_ID + "\n");

                for(int i = 1; i < headerList.size() - 1; ++i) {
                    text.append("N" + ((FPGrowth.FPTreeNode)headerList.get(i)).m_ID + "->N" + ((FPGrowth.FPTreeNode)headerList.get(i + 1)).m_ID + "\n");
                }

                ++maxID;
            }
        }

        protected static class Header implements Serializable {
            private static final long serialVersionUID = -6583156284891368909L;
            protected List<FPGrowth.FPTreeNode> m_headerList = new LinkedList();
            protected FPGrowth.ShadowCounts m_projectedHeaderCounts = new FPGrowth.ShadowCounts();

            protected Header() {
            }

            public void addToList(FPGrowth.FPTreeNode toAdd) {
                this.m_headerList.add(toAdd);
            }

            public List<FPGrowth.FPTreeNode> getHeaderList() {
                return this.m_headerList;
            }

            public FPGrowth.ShadowCounts getProjectedCounts() {
                return this.m_projectedHeaderCounts;
            }
        }
    }

    protected static class FPTreeNode implements Serializable {
        private static final long serialVersionUID = 4396315323673737660L;
        protected FPGrowth.FPTreeNode m_levelSibling;
        protected FPGrowth.FPTreeNode m_parent;
        protected BinaryItem m_item;
        protected int m_ID;
        protected Map<BinaryItem, FPGrowth.FPTreeNode> m_children = new HashMap();
        protected FPGrowth.ShadowCounts m_projectedCounts = new FPGrowth.ShadowCounts();

        public FPTreeNode(FPGrowth.FPTreeNode parent, BinaryItem item) {
            this.m_parent = parent;
            this.m_item = item;
        }

        public void addItemSet(Collection<BinaryItem> itemSet, Map<BinaryItem, FPGrowth.FPTreeRoot.Header> headerTable, int incr) {
            Iterator<BinaryItem> i = itemSet.iterator();
            if (i.hasNext()) {
                BinaryItem first = (BinaryItem)i.next();
                FPGrowth.FPTreeNode aChild;
                if (!this.m_children.containsKey(first)) {
                    aChild = new FPGrowth.FPTreeNode(this, first);
                    this.m_children.put(first, aChild);
                    if (!headerTable.containsKey(first)) {
                        headerTable.put(first, new FPGrowth.FPTreeRoot.Header());
                    }

                    ((FPGrowth.FPTreeRoot.Header)headerTable.get(first)).addToList(aChild);
                } else {
                    aChild = (FPGrowth.FPTreeNode)this.m_children.get(first);
                }

                ((FPGrowth.FPTreeRoot.Header)headerTable.get(first)).getProjectedCounts().increaseCount(0, incr);
                aChild.increaseProjectedCount(0, incr);
                itemSet.remove(first);
                aChild.addItemSet(itemSet, headerTable, incr);
            }

        }

        public void increaseProjectedCount(int recursionLevel, int incr) {
            this.m_projectedCounts.increaseCount(recursionLevel, incr);
        }

        public void removeProjectedCount(int recursionLevel) {
            this.m_projectedCounts.removeCount(recursionLevel);
        }

        public int getProjectedCount(int recursionLevel) {
            return this.m_projectedCounts.getCount(recursionLevel);
        }

        public FPGrowth.FPTreeNode getParent() {
            return this.m_parent;
        }

        public BinaryItem getItem() {
            return this.m_item;
        }

        public String toString(int recursionLevel) {
            return this.toString("", recursionLevel);
        }

        public String toString(String prefix, int recursionLevel) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(prefix);
            buffer.append("|  ");
            buffer.append(this.m_item.toString());
            buffer.append(" (");
            buffer.append(this.m_projectedCounts.getCount(recursionLevel));
            buffer.append(")\n");
            Iterator var4 = this.m_children.values().iterator();

            while(var4.hasNext()) {
                FPGrowth.FPTreeNode node = (FPGrowth.FPTreeNode)var4.next();
                buffer.append(node.toString(prefix + "|  ", recursionLevel));
            }

            return buffer.toString();
        }

        protected int assignIDs(int lastID) {
            int currentLastID = lastID + 1;
            this.m_ID = currentLastID;
            if (this.m_children != null) {
                Collection<FPGrowth.FPTreeNode> kids = this.m_children.values();

                FPGrowth.FPTreeNode n;
                for(Iterator var4 = kids.iterator(); var4.hasNext(); currentLastID = n.assignIDs(currentLastID)) {
                    n = (FPGrowth.FPTreeNode)var4.next();
                }
            }

            return currentLastID;
        }

        public void graphFPTree(StringBuffer text) {
            if (this.m_children != null) {
                Collection<FPGrowth.FPTreeNode> kids = this.m_children.values();
                Iterator var3 = kids.iterator();

                while(var3.hasNext()) {
                    FPGrowth.FPTreeNode n = (FPGrowth.FPTreeNode)var3.next();
                    text.append("N" + n.m_ID);
                    text.append(" [label=\"");
                    text.append(n.getItem().toString() + " (" + n.getProjectedCount(0) + ")\\n");
                    text.append("\"]\n");
                    n.graphFPTree(text);
                    text.append("N" + this.m_ID + "->N" + n.m_ID + "\n");
                }
            }

        }
    }

    protected static class ShadowCounts implements Serializable {
        private static final long serialVersionUID = 4435433714185969155L;
        private ArrayList<Integer> m_counts = new ArrayList();

        protected ShadowCounts() {
        }

        public int getCount(int recursionLevel) {
            return recursionLevel >= this.m_counts.size() ? 0 : (Integer)this.m_counts.get(recursionLevel);
        }

        public void increaseCount(int recursionLevel, int incr) {
            if (recursionLevel == this.m_counts.size()) {
                this.m_counts.add(incr);
            } else if (recursionLevel == this.m_counts.size() - 1) {
                int n = (Integer)this.m_counts.get(recursionLevel);
                this.m_counts.set(recursionLevel, n + incr);
            }

        }

        public void removeCount(int recursionLevel) {
            if (recursionLevel < this.m_counts.size()) {
                this.m_counts.remove(recursionLevel);
            }

        }
    }

    protected static class FrequentItemSets implements Serializable {
        private static final long serialVersionUID = 4173606872363973588L;
        protected ArrayList<FPGrowth.FrequentBinaryItemSet> m_sets = new ArrayList();
        protected int m_numberOfTransactions;

        public FrequentItemSets(int numTransactions) {
            this.m_numberOfTransactions = numTransactions;
        }

        public FPGrowth.FrequentBinaryItemSet getItemSet(int index) {
            return (FPGrowth.FrequentBinaryItemSet)this.m_sets.get(index);
        }

        public Iterator<FPGrowth.FrequentBinaryItemSet> iterator() {
            return this.m_sets.iterator();
        }

        public int getNumberOfTransactions() {
            return this.m_numberOfTransactions;
        }

        public void addItemSet(FPGrowth.FrequentBinaryItemSet setToAdd) {
            this.m_sets.add(setToAdd);
        }

        public void sort(Comparator<FPGrowth.FrequentBinaryItemSet> comp) {
            Collections.sort(this.m_sets, comp);
        }

        public int size() {
            return this.m_sets.size();
        }

        public void sort() {
            Comparator<FPGrowth.FrequentBinaryItemSet> compF = new Comparator<FPGrowth.FrequentBinaryItemSet>() {
                public int compare(FPGrowth.FrequentBinaryItemSet one, FPGrowth.FrequentBinaryItemSet two) {
                    Collection<BinaryItem> compOne = one.getItems();
                    Collection<BinaryItem> compTwo = two.getItems();
                    if (compOne.size() < compTwo.size()) {
                        return -1;
                    } else if (compOne.size() > compTwo.size()) {
                        return 1;
                    } else {
                        Iterator<BinaryItem> twoIterator = compTwo.iterator();
                        Iterator var6 = compOne.iterator();

                        int result;
                        do {
                            if (!var6.hasNext()) {
                                return 0;
                            }

                            BinaryItem oneI = (BinaryItem)var6.next();
                            BinaryItem twoI = (BinaryItem)twoIterator.next();
                            result = oneI.compareTo(twoI);
                        } while(result == 0);

                        return result;
                    }
                }
            };
            this.sort(compF);
        }

        public String toString(int numSets) {
            if (this.m_sets.size() == 0) {
                return "No frequent items sets found!";
            } else {
                StringBuffer result = new StringBuffer();
                result.append("" + this.m_sets.size() + " frequent item sets found");
                if (numSets > 0) {
                    result.append(" , displaying " + numSets);
                }

                result.append(":\n\n");
                int count = 0;

                for(Iterator var4 = this.m_sets.iterator(); var4.hasNext(); ++count) {
                    FPGrowth.FrequentBinaryItemSet i = (FPGrowth.FrequentBinaryItemSet)var4.next();
                    if (numSets > 0 && count > numSets) {
                        break;
                    }

                    result.append(i.toString() + "\n");
                }

                return result.toString();
            }
        }
    }

    protected static class FrequentBinaryItemSet implements Serializable, Cloneable {
        private static final long serialVersionUID = -6543815873565829448L;
        protected ArrayList<BinaryItem> m_items = new ArrayList();
        protected int m_support;

        public FrequentBinaryItemSet(ArrayList<BinaryItem> items, int support) {
            this.m_items = items;
            this.m_support = support;
            Collections.sort(this.m_items);
        }

        public void addItem(BinaryItem i) {
            this.m_items.add(i);
            Collections.sort(this.m_items);
        }

        public void setSupport(int support) {
            this.m_support = support;
        }

        public int getSupport() {
            return this.m_support;
        }

        public Collection<BinaryItem> getItems() {
            return this.m_items;
        }

        public BinaryItem getItem(int index) {
            return (BinaryItem)this.m_items.get(index);
        }

        public int numberOfItems() {
            return this.m_items.size();
        }

        public String toString() {
            StringBuffer buff = new StringBuffer();
            Iterator i = this.m_items.iterator();

            while(i.hasNext()) {
                buff.append(((BinaryItem)i.next()).toString() + " ");
            }

            buff.append(": " + this.m_support);
            return buff.toString();
        }

        public Object clone() {
            ArrayList<BinaryItem> items = new ArrayList(this.m_items);
            return new FPGrowth.FrequentBinaryItemSet(items, this.m_support);
        }
    }
}
