import groovy.io.FileType
import groovy.json.JsonSlurper

def folderWithRecommendationFiles = new File('./recommendations')
resultMap = [:]
minimumAmount = 0

folderWithRecommendationFiles.eachFileRecurse (FileType.FILES) { file ->
    minimumAmount+=80
    readFile(file)
    sortMapByValueDesc();
}
printResult();

def readFile(File file) {
    def jsonSlurper = new JsonSlurper()
    def jsonTree = jsonSlurper.parseText(file.text)
    int score = 100

    jsonTree.infoLink.each {
        infoLinkList->infoLinkList.each{ entry ->
            int currentScore = resultMap[entry]?:0
            resultMap[entry] = currentScore + score
            score--
        }
    }
}

def printResult() {
    resultMap.each{ entry ->
        if(entry.value >= minimumAmount) {
            println("${entry.key}: ${entry.value}")
        }
    }
}

def sortMapByValueDesc() {
    resultMap = resultMap.sort { -it.value }
}