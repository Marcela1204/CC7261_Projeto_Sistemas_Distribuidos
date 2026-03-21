import zmq

context = zmq.Context()

pub = context.socket(zmq.XPUB)
pub.bind("tcp://*:6666")

sub = context.socket(zmq.XSUB)
sub.bind("tcp://*:6665")

zmq.proxy(pub, sub)

pub.close()
sub.close()
context.close()
