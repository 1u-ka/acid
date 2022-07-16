package exocortex.buffer;

/**
 * ?
 *
 * @todo   clj code instantiates this object and calls its
 *         methods to:
 *           - read a sequence of events from the filesystem
 *           - write events to the filesystem
 */
class Buffer implements Persistable, Readable {

  /** */
  private path;

  /** */
  Buffer(String path) {
    this.path = path
  }

  /** */
  void push(ExtendedDataNotationObject event) {
    // 
  }
}