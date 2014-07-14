package shared


case class Hangman(
  level: Int = 0,
  word: String = "",
  guess: List[Char] = Nil,
  misses: Int = 0
){

  def guessWord = {
    for(c <- word.toCharArray) yield {
      if(guess.contains(c)) c
      else '_'
    }
  }

  def gameOver: Boolean = {
    (misses >= level) || won
  }

  def won = {
    guess.mkString("") == word
  }
}

