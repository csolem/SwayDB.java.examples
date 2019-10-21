package functions;


import org.junit.jupiter.api.Test;
import swaydb.java.MapIO;
import swaydb.java.PureFunction;
import swaydb.java.Return;
import swaydb.java.memory.Map;

import static org.junit.jupiter.api.Assertions.*;


import java.util.stream.IntStream;

import static swaydb.java.serializers.Default.intSerializer;
import static swaydb.java.serializers.Default.stringSerializer;

class LikesTest {

  @Test
  void likesCountTest() {

    MapIO<String, Integer, PureFunction<String, Integer, Return.Map<Integer>>> likesMap =
      Map.configWithFunctions(stringSerializer(), intSerializer())
        .init()
        .get();

    likesMap.put("SwayDB", 0); //initial entry with 0 likes.

    //function that increments likes by 1
    //in SQL this would be "UPDATE LIKES_TABLE SET LIKES = LIKES + 1"
    PureFunction.OnValue<String, Integer, Return.Map<Integer>> incrementLikesFunction =
      currentLikes ->
        Return.update(currentLikes + 1);

    //register the above likes function
    likesMap.registerFunction(incrementLikesFunction).get();

    //this could also be applied concurrently and the end result is the same.
    //applyFunction is atomic and thread-safe.
    IntStream
      .rangeClosed(1, 100)
      .forEach(
        integer ->
          likesMap.applyFunction("SwayDB", incrementLikesFunction).get()
      );

    //assert the number of likes applied.
    assertEquals(100, likesMap.get("SwayDB").get().get());
  }
}
