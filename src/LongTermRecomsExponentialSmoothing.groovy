import groovy.io.FileType
import groovy.json.JsonSlurper

def folderWithRecommendationFiles = new File('./recommendations')
dataFromFile = [:]
allResults = [:]

folderWithRecommendationFiles.eachFileRecurse (FileType.FILES) { file ->
    readFile(file)
    fillZeroValues()
}
fillMissingZeroValuesUpfront()
createExponentialSmoothing()
sortMapByValueDesc();
printResult();

def readFile(File file) {
    def jsonTree = new JsonSlurper().parseText(file.text)
    int score = 100

    jsonTree.infoLink.each {
        infoLinkList->infoLinkList.each{ entry ->
            currentScoreListForThisEntry = dataFromFile[entry] ?: []
            dataFromFile[entry] = currentScoreListForThisEntry << score
            score--
        }
    }
}

def determineMaxSize() {
    int max = 0

    dataFromFile.each { dataFromFileEntry ->
        int entrySize = dataFromFileEntry.value.size()

        if(entrySize > max) {
            max = entrySize
        }
    }

    return max
}

def fillZeroValues() {
    int maxSize = determineMaxSize()

    dataFromFile.each { dataFromFileEntry ->
        if(dataFromFileEntry.value.size() < maxSize) {
            dataFromFileEntry.value << 0
        }
    }
}

def fillMissingZeroValuesUpfront() {
    int maxSize = determineMaxSize()

    dataFromFile.each { dataFromFileEntry ->
        while(dataFromFileEntry.value.size() < maxSize) {
            dataFromFile[dataFromFileEntry.key] = dataFromFileEntry.value.plus(0, 0)
        }
    }
}

def createExponentialSmoothing() {
    dataFromFile.each { entry->
        smoothEntry(entry)
    }
}

def smoothEntry(entry) {
    tMinusOne = null

    for(int index = 0; index < entry.value.size(); index++) {
        currentValueOfEntry = entry.value.get(index)

        if(tMinusOne == null) {
            tMinusOne = currentValueOfEntry
        } else {
            tMinusOne = 0.3 * currentValueOfEntry + 0.7 * tMinusOne
        }
    }

    allResults[entry.key] = tMinusOne
}

def printResult() {
    allResults.each{ entry ->
        if(entry.value >= 80) {
            println("${entry.key}: ${entry.value}")
        }
    }
}

def sortMapByValueDesc() {
    allResults = allResults.sort { -it.value }
}