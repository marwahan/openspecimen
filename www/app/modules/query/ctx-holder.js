angular.module("os.query.ctxholder", [])
  .factory("QueryCtxHolder", function() {
     this.queryCtx = undefined;

     return {
       getQueryCtx: function() {
         return this.queryCtx;
       },

       setQueryCtx: function(queryCtx) {
         this.queryCtx = queryCtx
       }
     }
  })
