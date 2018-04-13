# rspace-xstream
Default [XStream](http://x-stream.github.io/) serialization for [RChain](https://developer.rchain.coop)'s [RSpace](https://github.com/rchain/rchain/tree/master/rspace)

Implements default XStream serializers to be passed to the RSpace library.

To override the default xstream functionality, you can pass in a custom [XStream object](http://x-stream.github.io/javadoc/com/thoughtworks/xstream/XStream.html).

Also, if you want to want to override the default xstream serialization for certain objects, you can still pass custom [`Serialize`](https://github.com/rchain/rchain/blob/master/rspace/src/main/scala/coop/rchain/rspace/Serialize.scala#L11) objects to `consume` and `produce`.
