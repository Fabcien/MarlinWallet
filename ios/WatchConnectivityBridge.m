#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_REMAP_MODULE(WatchConnectivity, WatchConnectivityModule, NSObject)

RCT_EXTERN_METHOD(sendDataToWatch:(NSString *)address bip21Prefix:(NSString *)bip21Prefix)

@end

