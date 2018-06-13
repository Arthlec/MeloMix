package projet_e3.esiee.com.projet_e3;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToBinary;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingWithUserConstant;

public abstract class AnalyseData extends AppCompatActivity {

    /**
     * Pour appeler analyseData(this.getFilesDir());
     */
    private Instances histoBase;

    public ArrayList<String> analyseData(File rootDataDir){
        File[] files = this.getJSONFiles(rootDataDir);
        int numberOfUsers = files.length;
        ArrayList<Attribute> attributeArrayList = this.getAttributesArrayList(files);
        int numberOfGenres = attributeArrayList.size();
        Instances dataBase = createDatabase(files, numberOfUsers, numberOfGenres, attributeArrayList);
        dataBase = this.replaceDatabaseMissingValuesWithZeros(dataBase);

        SimpleKMeans simpleKMeans = new SimpleKMeans();
        this.executeKmeans(numberOfUsers,simpleKMeans,dataBase, 10, 0.33, 0.66);
        Instances centroidsBase = simpleKMeans.getClusterCentroids();
        int centroidsBaseSize = centroidsBase.size();

        ArrayList<Integer> indexOfAttributeToKeep = createListIndexOfAttributeToKeep(centroidsBaseSize, centroidsBase, 0.10);
        Instances centroidsBaseSimplified = createDatabaseSimplified(indexOfAttributeToKeep, centroidsBase);

        if(centroidsBaseSize<=1){
            Instances OneCentroidBase = new Instances(centroidsBaseSimplified);
            OneCentroidBase = this.binarizedDatabase(OneCentroidBase, 0.02);
            return getFrequentGenresOneUser(OneCentroidBase);
        }

        centroidsBaseSimplified = this.binarizedDatabase(centroidsBaseSimplified, 0.01);

        FPGrowth algo = this.createAlgo(centroidsBaseSimplified, 5, 1, -2.0, 1.4, 0.3);
        int sizeItemsets = algo.m_largeItemSets.size();
        ArrayList<String> frequentGenres = this.getFrequentGenresList(algo, sizeItemsets);

        return frequentGenres;
    }

    private ArrayList<String> getFrequentGenresOneUser(Instances OneCentroidBase){
        ArrayList<String> oneUserGenre = new ArrayList<>();
        int len = OneCentroidBase.numAttributes();
        for(int i=0;i<len;i++){
            Attribute currentAttribut = OneCentroidBase.attribute(i);
            Log.i("current",currentAttribut+"");
            Log.i("value",OneCentroidBase.get(0).value(currentAttribut)+"");
            if(OneCentroidBase.get(0).value(currentAttribut)!=0){
               oneUserGenre.add(currentAttribut.name());
               Log.i("name",currentAttribut.name());
            }
        }
        Log.i("List",oneUserGenre+"");
        oneUserGenre = applyRegex(oneUserGenre,"_binarized");
        return  oneUserGenre;
    }

    private ArrayList<String> applyRegex(ArrayList<String> list, String regex){
        ArrayList<String> regexList = new ArrayList<>();
        int listSize = list.size();
        for(int i=0; i<listSize; i++){
            String currentGenreName = list.get(i);
            Log.i("currenName",currentGenreName);
            currentGenreName = currentGenreName.replaceAll(regex, "");
            regexList.add(i, currentGenreName);
        }
        Log.i("regex",regexList+"");
        return  regexList;
    }

    private ArrayList<String> getFrequentGenresList(FPGrowth algo, int sizeItemsets){
        ArrayList<FPGrowth.FrequentBinaryItemSet> itemSetArrayList = this.getItemSetArrayList(algo, sizeItemsets);
        ArrayList<FPGrowth.FrequentBinaryItemSet> itemSetArrayListMax = this.getItemSetArrayListMax(sizeItemsets, itemSetArrayList);
        ArrayList<String> frequentGenres = new ArrayList<>();
        int itemSetArrayListMaxSize = itemSetArrayListMax.size();

        for(int i=0; i<itemSetArrayListMaxSize; i++){
            FPGrowth.FrequentBinaryItemSet currentFrequentItemSet = itemSetArrayListMax.get(i);
            int currentFrequentItemSetSize = currentFrequentItemSet.numberOfItems();
            for(int j=0; j<currentFrequentItemSetSize; j++){
                String genreName = currentFrequentItemSet.getItem(j).toString();
                if(!frequentGenres.contains(genreName))
                    frequentGenres.add(genreName);
            }
        }
        String regex = "_binarized=1";
        frequentGenres = applyRegex(frequentGenres,regex);
        return frequentGenres;
    }

    private ArrayList<FPGrowth.FrequentBinaryItemSet> getItemSetArrayListMax(int sizeItemsets, ArrayList<FPGrowth.FrequentBinaryItemSet> itemSetArrayList){
        int maxSupport = Integer.MIN_VALUE;
        int maxNumberOfItems = Integer.MIN_VALUE;
        for(int i=0; i<sizeItemsets; i++){
            FPGrowth.FrequentBinaryItemSet currentFrequentItemSet = itemSetArrayList.get(i);
            int currentFrequentItemSetSupport = currentFrequentItemSet.getSupport();
            int currentFrequentItemSetLength = currentFrequentItemSet.numberOfItems();
            if(maxSupport < currentFrequentItemSetSupport)
                maxSupport = currentFrequentItemSetSupport;
            if(maxNumberOfItems < currentFrequentItemSetLength)
                maxNumberOfItems = currentFrequentItemSetLength;
        }

        ArrayList<FPGrowth.FrequentBinaryItemSet> itemSetArrayListMax = new ArrayList<>();
        for(int i=0; i<sizeItemsets; i++){
            FPGrowth.FrequentBinaryItemSet currentFrequentItemSet = itemSetArrayList.get(i);
            int currentFrequentItemSetSupport = currentFrequentItemSet.getSupport();
            int currentFrequentItemSetLength = currentFrequentItemSet.numberOfItems();
            if(maxSupport == currentFrequentItemSetSupport && maxNumberOfItems == currentFrequentItemSetLength)
                itemSetArrayListMax.add(currentFrequentItemSet);
        }
        return itemSetArrayListMax;
    }

    private ArrayList<FPGrowth.FrequentBinaryItemSet> getItemSetArrayList(FPGrowth algo, int sizeItemsets){
        Iterator<FPGrowth.FrequentBinaryItemSet> itemsets = algo.m_largeItemSets.iterator();
        Log.i("frequentItemsetsSize", "" + sizeItemsets);
        ArrayList<FPGrowth.FrequentBinaryItemSet> itemSetArrayList = new ArrayList<FPGrowth.FrequentBinaryItemSet>();
        while(itemsets.hasNext()){
            FPGrowth.FrequentBinaryItemSet currentFrequentItemSet = itemsets.next();
            itemSetArrayList.add(currentFrequentItemSet);
            Log.i("itemset", currentFrequentItemSet.toString());
        }
        return itemSetArrayList;
    }

    private FPGrowth createAlgo(Instances centroidsBaseSimplified, int maxNumberOfItems, int numRulesToFind, double a, double b, double c){
        int centroidsBaseSimplifiedSize = centroidsBaseSimplified.size();
        FPGrowth algo = new FPGrowth();
        algo.setMaxNumberOfItems(maxNumberOfItems);
        //algo.setNumRulesToFind(numRulesToFind);
        algo.setLowerBoundMinSupport(Math.exp(a*centroidsBaseSimplifiedSize + b) + c); //on définit le seuil minimum (pourcentage)
        try {
            algo.buildAssociations(centroidsBaseSimplified);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return algo;
    }

    private Instances binarizedDatabase(Instances centroidsBaseSimplified, double threshold){
        //Log.i("centroidsBaseSimplified", centroidsBaseSimplified.toString());
        //numberOfGenres = indexOfAttributeToKeep.size();
        int numberOfGenres = centroidsBaseSimplified.numAttributes();
        int centroidsBaseSize = centroidsBaseSimplified.size();
        for(int i=0; i<centroidsBaseSize; i++){
            Instance currentInstance = centroidsBaseSimplified.get(i);
            for(int j = 0; j<numberOfGenres; j++){
                double currentAttributeValue = currentInstance.value(j);
                if((currentAttributeValue-threshold) <= 0.00001)
                    currentInstance.setValue(j, 0);
                else
                    currentInstance.setValue(j, 1);
            }
        }
        //Log.i("centroidsBaseBinarized", centroidsBaseSimplified.toString());
        NumericToBinary filterBinary = new NumericToBinary();
        try {
            filterBinary.setInputFormat(centroidsBaseSimplified);
            centroidsBaseSimplified = Filter.useFilter(centroidsBaseSimplified, filterBinary);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return centroidsBaseSimplified;
    }

    private Instances createDatabaseSimplified(ArrayList<Integer> indexOfAttributeToKeep, Instances centroidsBase){
        Remove removeFilter = new Remove();
        int[] indexOfAttributeToKeepArray = convertIntegers(indexOfAttributeToKeep);
        removeFilter.setAttributeIndicesArray(indexOfAttributeToKeepArray);
        removeFilter.setInvertSelection(true);
        Instances centroidsBaseSimplified = null;
        try {
            removeFilter.setInputFormat(centroidsBase);
            centroidsBaseSimplified = Filter.useFilter(centroidsBase, removeFilter);
            histoBase = new Instances(centroidsBaseSimplified);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return centroidsBaseSimplified;
    }

    private ArrayList<Integer> createListIndexOfAttributeToKeep(int centroidsBaseSize, Instances centroidsBase, double percentOfAttributeToKeep){
        ArrayList<Integer> indexOfAttributeToKeep = new ArrayList<Integer>();
        for(int i=0; i<centroidsBaseSize; i++){
            Instance currentInstance = new SparseInstance(centroidsBase.get(i));
            //Log.i("instanceAvant", currentInstance.toString());
            int numberAttributes = currentInstance.numAttributes();
            HashMap<Integer, Double> attributeHashMap = new HashMap<>();
            for(int p = 0; p<numberAttributes; p++){
                attributeHashMap.put(p, currentInstance.value(p));
            }
            LinkedHashMap<Integer, Double> sortedHashMap = sortHashMapByValues(attributeHashMap);
            Iterator<Integer> hashIterator = sortedHashMap.keySet().iterator();
            int sortedHashMapSize = sortedHashMap.size();
            for(int j = 0;hashIterator.hasNext() && j < Math.round(sortedHashMapSize*percentOfAttributeToKeep); j++){
                Integer currentInt = hashIterator.next();
                if(!indexOfAttributeToKeep.contains(currentInt))
                    indexOfAttributeToKeep.add(currentInt);
            }
            //Log.i("instanceApres", currentInstance.toString());
        }
        return indexOfAttributeToKeep;
    }

    private void executeKmeans(int numberOfUsers, SimpleKMeans simpleKMeans, Instances dataBase, int numberOfIterations, double a, double b){
        try {
            if(numberOfUsers == 2)
                simpleKMeans.setNumClusters(2);
            else
                simpleKMeans.setNumClusters((int)Math.round(a*numberOfUsers + b));

            simpleKMeans.setMaxIterations(numberOfIterations);
            simpleKMeans.setPreserveInstancesOrder(true);
            simpleKMeans.buildClusterer(dataBase);
            int[] assignments = simpleKMeans.getAssignments();
            int i = 0;
            for(int clusterNum : assignments) {
                Log.i("Instance " + i, " -> Cluster " + clusterNum + "\n");
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instances replaceDatabaseMissingValuesWithZeros(Instances dataBase){
        ReplaceMissingWithUserConstant filterMissingValues = new ReplaceMissingWithUserConstant();
        filterMissingValues.setNumericReplacementValue("0.0");
        try {
            filterMissingValues.setInputFormat(dataBase);
            dataBase = Filter.useFilter(dataBase, filterMissingValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataBase;
    }

    private Instances createDatabase(File[] files, int numberOfUsers, int numberOfGenres, ArrayList<Attribute> attributeArrayList){
        Instances dataBase = new Instances("usersGenres", attributeArrayList, numberOfUsers);
        for (int i = 0; i < numberOfUsers; i++) {
            Map<Object,Object> map = null;
            try {
                map = JSON.std.mapFrom(files[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Instance user = new SparseInstance(numberOfGenres);
            for(int p = 0; p<attributeArrayList.size(); p++){
                Attribute currentAttribute = attributeArrayList.get(p);
                String attributeName = currentAttribute.name();
                if(map.containsKey(attributeName)){
                    if(map.get(attributeName) != null)
                        user.setValue(currentAttribute, (double) map.get(attributeName));
                }
            }
            dataBase.add(user);
        }
        return  dataBase;
    }

    private ArrayList<Attribute> getAttributesArrayList(File[] files){
        ArrayList<Attribute> attributeArrayList = new ArrayList<Attribute>();
        int numberOfUsers = files.length;
        for (int i = 0; i < numberOfUsers; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            Map<Object,Object> map = null;
            try {
                map = JSON.std.mapFrom(files[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Log.i("mapKeyset", map.keySet().toString());
            //Log.i("mapValues", map.values().toString());
            for(Map.Entry<Object, Object> entry : map.entrySet()){
                Attribute genre = new Attribute((String)entry.getKey());
                if(!attributeArrayList.contains(genre)){
                    attributeArrayList.add(genre);
                }
            }
        }
        return attributeArrayList;
    }

    /**
     * @param rootDataDir
     * @return JSONFiles in the rootDataDirectory
     */
    private File[] getJSONFiles(File rootDataDir){
        //File rootDataDir = this.getFilesDir();
        FilenameFilter jsonFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".json");
            }
        };
        File[] files = rootDataDir.listFiles(jsonFilter);
        return files;
    }

    /**
     * Convert an ArrayList of Integers to an int array
     * @param integers
     * @return an int array
     */
    private int[] convertIntegers(ArrayList<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i);
        }
        return ret;
    }

    /**
     * Sort a HashMap by its values
     * @param passedMap
     * @return a LinkedHashMap clone of the parameter
     */
    private LinkedHashMap<Integer, Double> sortHashMapByValues(HashMap<Integer, Double> passedMap) {
        List<Integer> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        Collections.reverse(mapKeys);

        LinkedHashMap<Integer, Double> sortedMap =
                new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<Integer> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Integer key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (Math.abs(comp1 - comp2) <= 0.000000000001) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public List[] buildListTab(){
        List[] ListTab = new List[2];
        List<String> AttributeList = new ArrayList<>();
        List<Double> ValuesList = new ArrayList<>();
        //Log.i("histo",histoBase.toString());
        int size = histoBase.size();
        for (int i=0;i<size;i++){
            int len = histoBase.get(i).numAttributes();
            for(int j=0;j<len;j++){
                Attribute currentAttribut = histoBase.get(i).attribute(j);
                //Log.i("value",histoBase.get(i).value(currentAttribut)+"");
                Double value = histoBase.get(i).value(currentAttribut);
                //Log.i("value",currentAttribut.name()+"  "+value.toString());
                histoBase.get(i).attribute(j).setWeight(currentAttribut.weight()+value);
                //Log.i("wieght",currentAttribut.name()+"   "+currentAttribut.weight()+"");
            }
        }
        for(int att=0;att<histoBase.numAttributes();att++){
            AttributeList.add(histoBase.attribute(att).name());
            ValuesList.add(histoBase.attribute(att).weight()-1);
        }
        ListTab[0] = AttributeList;
        ListTab[1] = ValuesList;
        return ListTab;
    }


}
