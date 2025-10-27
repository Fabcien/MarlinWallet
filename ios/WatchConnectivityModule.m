#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(WatchConnectivity, NSObject)

RCT_EXTERN_METHOD(sendDataToWatch:(NSString *)address bip21Prefix:(NSString *)bip21Prefix)

@end

